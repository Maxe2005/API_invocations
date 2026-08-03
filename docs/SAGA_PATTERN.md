# Pattern Saga - Gestion des Transactions Distribuées

## Contexte

Dans une architecture microservices, les transactions ACID traditionnelles ne fonctionnent pas car elles ne peuvent pas s'étendre sur plusieurs services/bases de données. Le pattern Saga résout ce problème.

## Qu'est-ce qu'une Saga ?

Une **Saga** est une séquence de transactions locales où :
- Chaque transaction met à jour un seul service
- Les transactions sont coordonnées par des messages/événements
- En cas d'échec, des **transactions compensatoires** annulent les changements

## Types de Saga

### 1. Orchestration (utilisé dans notre implémentation)
Un orchestrateur central contrôle le flux :
```
Orchestrateur → Service A → Service B → Service C
```

**Avantages** :
- Logique centralisée, facile à comprendre
- Meilleur contrôle du flux
- Idéal pour les workflows complexes

**Inconvénients** :
- Point de défaillance unique
- Couplage avec l'orchestrateur

### 2. Chorégraphie
Chaque service écoute des événements et décide quoi faire :
```
Service A → Événement → Service B → Événement → Service C
```

## Notre Implémentation

### Séquence normale
```
┌───────────────┐
│ Client        │
└───────┬───────┘
        │ POST /api/invocation/global-invoque/{playerId}
        ▼
┌────────────────────────┐
│ InvocationService      │
│ (Orchestrateur)        │
└───────┬────────────────┘
        │
        │ 1. invoke() - Génère le monstre localement
        ▼
┌────────────────────────┐
│ MonsterService         │
└────────────────────────┘
        │
        │ 2. createMonster() - Crée dans API Monsters
        ▼
┌────────────────────────┐
│ MonstersApiClient      │───────→ API Monsters (POST /api/monsters/create)
└───────┬────────────────┘              │
        │                               ▼
        │ ◄──────────────────── {monsterId: "123"}
        │
        │ 3. addMonsterToPlayer() - Ajoute au joueur
        ▼
┌────────────────────────┐
│ PlayerApiClient        │───────→ API Joueur (POST /api/joueur/add_monster)
└────────────────────────┘              │
                                        ▼
                                   {success: true}
```

### Séquence de compensation (échec)
```
┌────────────────────────┐
│ InvocationService      │
└───────┬────────────────┘
        │
        │ 1. invoke() ✅
        │ 2. createMonster() ✅ → monsterId = "123"
        │ 3. addMonsterToPlayer() ❌ ÉCHEC !
        │
        │ catch (ExternalApiException)
        │
        │ COMPENSATION ↓
        ▼
┌────────────────────────┐
│ MonstersApiClient      │───────→ API Monsters (DELETE /api/monsters/123)
│ .deleteMonster("123")  │
└────────────────────────┘
```

## Buffer d'audit et de rejeu (`invocation_buffer`)

Contrairement à la version simplifiée ci-dessus, l'implémentation réelle **persiste chaque
invocation avant tout appel externe**, dans la table PostgreSQL `invocation_buffer`
(`InvocationBufferDto`, colonnes JSONB pour le snapshot du monstre et les requêtes/réponses
échangées). Ce buffer sert à la fois d'audit et de mécanisme de rejeu :

- Statuts : `PENDING` → `MONSTER_CREATED` → `COMPLETED` / `FAILED` / `ABANDONED`.
- `POST /api/invocation/recreate` rejoue toutes les entrées non `COMPLETED` (et non
  `ABANDONED`) en réutilisant le snapshot et les requêtes déjà persistées, sans consommer un
  nouveau tirage aléatoire.
- Un plafond configurable (`app.invocation.max-attempts`, défaut 5) fait passer une entrée en
  `ABANDONED` de façon définitive au-delà d'un certain nombre de tentatives (appel initial +
  rejeux), pour éviter un rejeu infini.

## Code de la méthode globalInvoke() (simplifié)

```java
public GlobalMonsterWithIdDto globalInvoke(String playerId) {
    logger.info("Début de l'invocation globale pour le joueur: {}", playerId);

    GlobalMonsterDto monster = invoke();
    CreateMonsterRequest monsterRequest = invocationServiceMapper.toCreateMonsterRequest(monster, playerId);

    // Persistance en buffer AVANT tout appel externe (statut PENDING)
    InvocationBufferDto bufferEntry = createBufferEntry(playerId, monster, monsterRequest);

    String createdMonsterId = executeInvocation(monster, playerId, bufferEntry);
    return invocationServiceMapper.toGlobalMonsterWithIdDto(monster, createdMonsterId);
}

// executeInvocation() (appelé aussi par replayBufferedInvocations()) :
// - vérifie le plafond de tentatives (sinon ABANDONED)
// - crée le monstre dans API Monsters, marque MONSTER_CREATED
// - ajoute le monstre au joueur via API Joueur, marque COMPLETED
// - en cas d'échec : marque FAILED, puis déclenche la compensation
//   (deleteMonster, retentée une fois si le premier essai échoue)
```

## Cas d'usage

### Cas 1 : Tout réussit ✅
```
invoke() ✅ → createMonster() ✅ → addMonsterToPlayer() ✅
Résultat : Monstre créé et ajouté au joueur
```

### Cas 2 : Échec à l'étape 2 ❌
```
invoke() ✅ → createMonster() ❌
Résultat : Aucune compensation nécessaire, monsterId = null
```

### Cas 3 : Échec à l'étape 3 ❌ (Le plus critique)
```
invoke() ✅ → createMonster() ✅ → addMonsterToPlayer() ❌
COMPENSATION : deleteMonster(monsterId)
Résultat : Monstre supprimé, cohérence restaurée
```

## Avantages de notre implémentation

1. **Cohérence éventuelle** : Même en cas d'échec partiel, le système reste cohérent
2. **Résilience** : Gère les pannes réseau, timeouts, erreurs API
3. **Traçabilité** : Logs détaillés à chaque étape
4. **Simplicité** : Pattern facile à comprendre et maintenir

## Limitations et améliorations futures

### Limitations actuelles
- **Compensation à retry unique** : `deleteMonster()` est retentée une fois immédiatement si le
  premier essai échoue ; si les deux échouent, le buffer est marqué avec une raison explicite
  mentionnant le monstre orphelin (traçable en base), mais sans retry différé ni file dédiée.
- **Pas de retry sur `createMonster`/`addMonsterToPlayer`** : une seule tentative par étape lors
  d'un appel donné (le rejeu via `POST /api/invocation/recreate` est manuel/périodique, pas un
  retry automatique immédiat) — voir Resilience4j ci-dessous.
- **Synchrone** : Bloque jusqu'à la fin de toutes les étapes.

### Améliorations possibles

#### 1. Retry avec Exponential Backoff
```java
@Retryable(
    value = {ExternalApiException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public String createMonster(GlobalMonsterDto monster) {
    // ...
}
```

#### 2. Circuit Breaker
```java
@CircuitBreaker(name = "monstersApi", fallbackMethod = "createMonsterFallback")
public String createMonster(GlobalMonsterDto monster) {
    // ...
}
```

#### 3. Saga asynchrone avec Event Sourcing
Utiliser Kafka/RabbitMQ pour décorréler les étapes :
```
InvocationService → Event: MonsterCreated → PlayerService
```

## Bibliothèques recommandées

- **Resilience4j** : Circuit breaker, retry, rate limiter
- **Spring Retry** : Gestion des retentatives
- **Axon Framework** : Framework CQRS/Event Sourcing avec support Saga
- **Eventuate** : Framework spécialisé pour les Sagas

## Références

- [Pattern: Saga](https://microservices.io/patterns/data/saga.html)
- [Microservices Patterns (Chris Richardson)](https://www.manning.com/books/microservices-patterns)
- [Spring Cloud Saga](https://spring.io/blog/2021/07/27/spring-cloud-stream-and-event-driven-microservices)
