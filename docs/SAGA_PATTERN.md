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

## Code de la méthode globalInvoke()

```java
public GlobalMonsterDto globalInvoke(String playerId) {
    logger.info("Début de l'invocation globale pour le joueur: {}", playerId);
    
    String createdMonsterId = null;
    
    try {
        // Étape 1: Invoquer un monstre localement
        GlobalMonsterDto monster = invoke();
        
        // Étape 2: Créer le monstre dans l'API Monsters
        createdMonsterId = monstersApiClient.createMonster(monster);
        
        // Étape 3: Ajouter le monstre au joueur
        playerApiClient.addMonsterToPlayer(playerId, createdMonsterId);
        
        return monster;
        
    } catch (ExternalApiException e) {
        // COMPENSATION: Supprimer le monstre si créé
        if (createdMonsterId != null) {
            monstersApiClient.deleteMonster(createdMonsterId);
        }
        throw e; // Propager l'erreur
    }
}
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
- **Compensation manuelle** : Si `deleteMonster()` échoue, incohérence possible
- **Pas de retry** : Une seule tentative par étape
- **Synchrone** : Bloque jusqu'à la fin de toutes les étapes

### Améliorations possibles

#### 1. Table de Saga Log
Persister l'état de chaque saga pour reprise en cas de crash :
```java
@Entity
public class SagaLog {
    private String sagaId;
    private String step;
    private String status; // PENDING, COMPLETED, COMPENSATING, FAILED
    private String compensationData;
}
```

#### 2. Retry avec Exponential Backoff
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

#### 3. Circuit Breaker
```java
@CircuitBreaker(name = "monstersApi", fallbackMethod = "createMonsterFallback")
public String createMonster(GlobalMonsterDto monster) {
    // ...
}
```

#### 4. Saga asynchrone avec Event Sourcing
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
