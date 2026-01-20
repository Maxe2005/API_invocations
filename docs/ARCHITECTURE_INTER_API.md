# Architecture de Communication Inter-API

## Vue d'ensemble

Cette architecture implémente un système de communication entre microservices avec gestion des transactions distribuées selon le pattern **Saga avec compensation**.

## Principes SOLID appliqués

### 1. **Single Responsibility Principle (SRP)**
- `MonstersApiClient` : Responsable uniquement de la communication avec l'API Monsters
- `PlayerApiClient` : Responsable uniquement de la communication avec l'API Joueur
- `InvocationService` : Orchestre la logique métier d'invocation
- `ExternalApiProperties` : Centralise la configuration des APIs externes

### 2. **Open/Closed Principle (OCP)**
- Architecture extensible : pour ajouter une nouvelle API externe, créez simplement un nouveau client sans modifier le code existant

### 3. **Liskov Substitution Principle (LSP)**
- Les clients API peuvent être mockés facilement pour les tests

### 4. **Interface Segregation Principle (ISP)**
- Chaque client expose uniquement les méthodes nécessaires à son domaine

### 5. **Dependency Inversion Principle (DIP)**
- Les services dépendent d'abstractions (injection de dépendances) plutôt que de classes concrètes

## Principe DRY

- **Configuration centralisée** : `ExternalApiProperties` évite la duplication des URLs
- **Client HTTP réutilisable** : `RestClientConfig` configure un seul `RestTemplate` pour toutes les APIs
- **Gestion d'erreurs centralisée** : `GlobalExceptionHandler` traite toutes les exceptions

## Architecture des composants

```
┌─────────────────────────────────────────────────────────────┐
│                    InvocationController                      │
│                   (Point d'entrée HTTP)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    InvocationService                         │
│           (Orchestration + Pattern Saga)                     │
└────┬──────────────────────┬───────────────────────┬─────────┘
     │                      │                       │
     ▼                      ▼                       ▼
┌─────────┐        ┌────────────────┐     ┌──────────────────┐
│ Monster │        │ MonstersApi    │     │  PlayerApi       │
│ Service │        │ Client         │     │  Client          │
└─────────┘        └────────────────┘     └──────────────────┘
                           │                       │
                           ▼                       ▼
                   ┌──────────────┐       ┌──────────────┐
                   │ API Monsters │       │ API Joueur   │
                   │ (Externe)    │       │ (Externe)    │
                   └──────────────┘       └──────────────┘
```

## Pattern Saga avec Compensation

### Flux normal (succès)
1. **Invocation locale** : Génération du monstre
2. **Création dans API Monsters** : Appel POST /api/monsters/create → reçoit `monsterId`
3. **Ajout au joueur** : Appel POST /api/joueur/add_monster avec `monsterId`

### Flux de compensation (échec)
Si l'étape 3 échoue :
- **Compensation automatique** : Suppression du monstre créé à l'étape 2 via DELETE /api/monsters/{monsterId}
- **Exception propagée** : L'erreur est remontée au contrôleur avec un code HTTP 502

## Gestion des erreurs

### Types d'erreurs gérées
- **Erreurs réseau** : Timeout, connexion refusée
- **Erreurs HTTP** : Codes 4xx, 5xx des APIs externes
- **Erreurs métier** : Le joueur ne peut plus recevoir de monstres

### Réponse en cas d'erreur
```json
{
  "errors": [
    {
      "code": 502,
      "message": "Erreur de communication avec une API externe: ..."
    }
  ]
}
```

## Configuration

### application.yml
```yaml
external:
  api:
    monsters-base-url: http://api_monsters:8080
    player-base-url: http://api_joueur:8080
    connection-timeout: 5000  # en millisecondes
    read-timeout: 5000        # en millisecondes
```

### Docker Compose
Les noms d'hôtes (`api_monsters`, `api_joueur`) doivent correspondre aux noms des services dans votre `docker-compose.yml`.

## Utilisation

### Endpoint
```
POST /api/invocation/global-invoque/{playerId}
```

### Exemple
```bash
curl -X POST http://localhost:8080/api/invocation/global-invoque/player123
```

### Réponse (succès)
```json
{
  "element": "FIRE",
  "hp": 100,
  "atk": 50,
  "def": 30,
  "vit": 20,
  "skills": [...]
}
```

## Avantages de cette architecture

1. **Résilience** : Compensation automatique en cas d'échec partiel
2. **Traçabilité** : Logs détaillés à chaque étape
3. **Maintenabilité** : Code organisé, responsabilités claires
4. **Testabilité** : Chaque composant peut être testé indépendamment
5. **Évolutivité** : Facile d'ajouter de nouvelles APIs externes

## Amélioration futures possibles

- **Circuit Breaker** : Utiliser Resilience4j pour éviter les appels répétés à une API défaillante
- **Retry Policy** : Réessayer automatiquement les appels échoués
- **Async Processing** : Utiliser des queues (RabbitMQ, Kafka) pour les opérations longues
- **Distributed Tracing** : Intégrer Sleuth/Zipkin pour tracer les appels inter-services
