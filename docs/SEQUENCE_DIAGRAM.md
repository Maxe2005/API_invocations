```mermaid
sequenceDiagram
    participant Client
    participant Controller as InvocationController
    participant Service as InvocationService
    participant MClient as MonstersApiClient
    participant PClient as PlayerApiClient
    participant APIMonsters as API Monsters
    participant APIJoueur as API Joueur

    Note over Client,APIJoueur: Flux normal (Succès)
    
    Client->>Controller: POST /api/invocation/global-invoque/player123
    Controller->>Service: globalInvoke("player123")
    
    Note over Service: Étape 1: Invocation locale
    Service->>Service: invoke()
    Service-->>Service: GlobalMonsterDto
    
    Note over Service: Étape 2: Création du monstre
    Service->>MClient: createMonster(monster)
    MClient->>APIMonsters: POST /api/monsters/create
    APIMonsters-->>MClient: {monsterId: "monster-456"}
    MClient-->>Service: "monster-456"
    
    Note over Service: Étape 3: Ajout au joueur
    Service->>PClient: addMonsterToPlayer("player123", "monster-456")
    PClient->>APIJoueur: POST /api/joueur/add_monster
    APIJoueur-->>PClient: {success: true}
    PClient-->>Service: void (success)
    
    Service-->>Controller: GlobalMonsterDto
    Controller-->>Client: 200 OK + Monster data

    Note over Client,APIJoueur: Flux avec échec et compensation
    
    Client->>Controller: POST /api/invocation/global-invoque/player789
    Controller->>Service: globalInvoke("player789")
    
    Service->>Service: invoke() ✅
    Service->>MClient: createMonster(monster) ✅
    MClient->>APIMonsters: POST /api/monsters/create
    APIMonsters-->>MClient: {monsterId: "monster-999"}
    MClient-->>Service: "monster-999"
    
    Service->>PClient: addMonsterToPlayer("player789", "monster-999")
    PClient->>APIJoueur: POST /api/joueur/add_monster
    APIJoueur-->>PClient: ❌ ERROR 400 (Joueur plein)
    PClient-->>Service: ExternalApiException ❌
    
    Note over Service: COMPENSATION !
    rect rgb(255, 200, 200)
        Service->>MClient: deleteMonster("monster-999")
        MClient->>APIMonsters: DELETE /api/monsters/monster-999
        APIMonsters-->>MClient: 200 OK (deleted)
        MClient-->>Service: void
    end
    
    Service-->>Controller: ExternalApiException
    Controller-->>Client: 502 Bad Gateway + Error details
```

## Légende

- ✅ Opération réussie
- ❌ Opération échouée
- 🔄 Compensation (rollback)

## Cas d'usage couverts

### 1. Succès complet
Toutes les étapes se passent bien, le monstre est créé et ajouté au joueur.

### 2. Échec à l'étape 2 (API Monsters down)
L'invocation locale réussit, mais la création dans l'API Monsters échoue.
→ Pas de compensation nécessaire (monsterId = null)

### 3. Échec à l'étape 3 (API Joueur refuse)
Le monstre est créé dans l'API Monsters, mais l'ajout au joueur échoue.
→ **Compensation déclenchée** : suppression du monstre créé

### 4. Échec de la compensation
Si la suppression du monstre échoue aussi :
→ Log d'erreur, mais l'exception originale est quand même propagée
→ Permet de traiter manuellement les orphelins plus tard
