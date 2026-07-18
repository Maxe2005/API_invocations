# Système d'Invocation

## Vue d'ensemble

Le système d'invocation permet de générer aléatoirement des monstres avec leurs compétences associées, en respectant des probabilités par rang de rareté. Le système utilise un **pattern Saga** pour garantir la cohérence des données lors des appels aux API externes.

## Rangs de rareté

Le système définit 4 rangs de rareté pour les monstres et les compétences :

| Rang | Taux de base | Description |
|------|--------------|-------------|
| **COMMON** | 50% (0.5) | Monstres et compétences communs |
| **RARE** | 30% (0.3) | Monstres et compétences rares |
| **EPIC** | 15% (0.15) | Monstres et compétences épiques |
| **LEGENDARY** | 5% (0.05) | Monstres et compétences légendaires |

## Probabilités dynamiques

### Principe de base

Les probabilités ne sont **pas fixes** ! Elles s'adaptent en temps réel en fonction des données disponibles en base.

Si un rang n'a plus de monstres ou de compétences disponibles, il est **automatiquement exclu** du tirage et les probabilités des autres rangs sont **recalculées proportionnellement**.

### Exemple de recalcul

Imaginons qu'il n'y ait plus de monstres LEGENDARY disponibles :

```
Taux disponibles :
- COMMON: 0.5
- RARE: 0.3
- EPIC: 0.15
- LEGENDARY: 0.0 (exclus)

Total disponible = 0.5 + 0.3 + 0.15 = 0.95

Probabilités réelles :
- COMMON: 0.5 / 0.95 ≈ 52.63%
- RARE: 0.3 / 0.95 ≈ 31.58%
- EPIC: 0.15 / 0.95 ≈ 15.79%
- LEGENDARY: 0%
```

### Algorithme de sélection

1. **Calcul du total** : Somme des taux de drop de tous les rangs ayant des données disponibles
2. **Tirage aléatoire** : Génération d'un nombre entre 0 et le total calculé
3. **Sélection cumulée** : Parcours des rangs disponibles en accumulant les taux jusqu'à atteindre/dépasser la valeur tirée

```java
// Exemple simplifié
float totalAvailableRate = sumOfAvailableRanks(); // ex: 0.95
float randomValue = random() * totalAvailableRate; // ex: 0.42
float cumulativeRate = 0f;

for (Rank rank : availableRanks) {
    cumulativeRate += rank.getDropRate();
    if (randomValue <= cumulativeRate) {
        return rank; // Ce rang est sélectionné
    }
}
```

## Processus d'invocation

### 1. Invocation simple (`invoke()`)

```
┌─────────────────────────────────────────┐
│ 1. Sélection du rang du monstre         │
│    → Probabilités dynamiques            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 2. Sélection aléatoire d'un monstre     │
│    → Parmi tous les monstres du rang    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 3. Sélection de 3 compétences           │
│    → Chaque skill a son propre tirage   │
│    → Les skills déjà pris sont exclus   │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 4. Retour du monstre complet            │
│    → GlobalMonsterDto                   │
└─────────────────────────────────────────┘
```

### 2. Sélection des compétences

Pour chaque monstre, **3 compétences** sont sélectionnées :

- Chaque compétence a **son propre tirage de rang** (indépendant du rang du monstre)
- Une fois une compétence sélectionnée, elle est **retirée du pool** pour éviter les doublons
- Si un rang n'a plus de compétences disponibles, il est exclu des prochains tirages
- Les compétences sont numérotées de 1 à 3

**Note importante** : Un monstre COMMON peut très bien avoir une compétence LEGENDARY !

## Invocation globale avec Pattern Saga

### 3. Invocation globale (`globalInvoke(playerId)`)

L'invocation globale coordonne les appels aux API externes (Monsters et Player) avec un système de **compensation automatique** en cas d'échec.

```
┌──────────────────────────────────────────┐
│ 1. Invocation du monstre (local)         │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│ 2. Création buffer (InvocationBufferDto) │
│    → Status: PENDING                     │
│    → Sauvegarde en base                  │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│ 3. Appel API Monsters (createMonster)    │
└──────────────┬───────────────────────────┘
               │
               ├─── ✅ Succès
               │    │
               │    ▼
               │    ┌────────────────────────────────────┐
               │    │ → Status: MONSTER_CREATED          │
               │    │ → Sauvegarde monsterId             │
               │    └────────────┬───────────────────────┘
               │                 │
               │                 ▼
               │    ┌────────────────────────────────────┐
               │    │ 4. Appel API Player (addMonster)   │
               │    └────────────┬───────────────────────┘
               │                 │
               │                 ├─── ✅ Succès
               │                 │    │
               │                 │    ▼
               │                 │    ┌──────────────────────┐
               │                 │    │ → Status: COMPLETED  │
               │                 │    │ → Invocation réussie │
               │                 │    └──────────────────────┘
               │                 │
               │                 └─── ❌ Échec
               │                      │
               │                      ▼
               │                      ┌────────────────────────────┐
               │                      │ 5. COMPENSATION            │
               │                      │ → Suppression du monstre   │
               │                      │ → Status: FAILED           │
               │                      └────────────────────────────┘
               │
               └─── ❌ Échec
                    │
                    ▼
                    ┌────────────────────────────┐
                    │ → Status: FAILED           │
                    │ → Pas de compensation      │
                    └────────────────────────────┘
```

### États du buffer d'invocation

| État | Description |
|------|-------------|
| **PENDING** | Invocation initiée, en attente |
| **MONSTER_CREATED** | Monstre créé avec succès dans l'API Monsters |
| **COMPLETED** | Monstre ajouté au joueur avec succès |
| **FAILED** | Échec de l'invocation (avec raison) |

### Mécanisme de compensation (Saga)

En cas d'échec lors de l'ajout du monstre au joueur :
1. Le monstre a déjà été créé dans l'API Monsters
2. Le système déclenche automatiquement la **compensation**
3. Le monstre est **supprimé** de l'API Monsters via `deleteMonster()`
4. Le buffer est marqué `FAILED` avec la raison de l'échec

⚠️ **Limite** : Si la compensation échoue également, une erreur est loggée mais l'opération continue.

## Replay d'invocations échouées

### 4. Rejeu automatique (`replayBufferedInvocations()`)

Le système peut **rejouer automatiquement** les invocations qui ont échoué :

```
┌──────────────────────────────────────────┐
│ 1. Récupération des invocations          │
│    "recréables" depuis le buffer         │
│    → Status: PENDING ou FAILED           │
└──────────────┬───────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────┐
│ 2. Pour chaque entrée                      │
│    → Exécution de executeInvocation()      │
│    → Incrémentation du compteur tentatives │
└──────────────┬─────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│ 3. Génération du rapport                 │
│    → Total traité                        │
│    → Nombre de succès                    │
│    → Nombre d'échecs                     │
│    → IDs des invocations échouées        │
└──────────────────────────────────────────┘
```

### Conditions de "recréabilité"

Une invocation peut être rejouée si :
- Elle a un snapshot du monstre sauvegardé
- Elle n'est pas déjà COMPLETED
- Le nombre de tentatives n'a pas atteint la limite (si définie)

## Persistance et traçabilité

Chaque invocation globale crée une entrée `InvocationBufferDto` contenant :

| Champ | Description |
|-------|-------------|
| `playerId` | ID du joueur concerné |
| `monsterSnapshot` | Copie complète du monstre invoqué |
| `monsterRequest` | Requête envoyée à l'API Monsters |
| `monsterResponse` | Réponse de l'API Monsters |
| `playerRequest` | Requête envoyée à l'API Player |
| `playerResponse` | Réponse de l'API Player |
| `status` | État actuel de l'invocation |
| `attemptCount` | Nombre de tentatives effectuées |
| `failureReason` | Raison de l'échec (si applicable) |
| `createdAt` | Date de création |
| `lastAttemptAt` | Date de la dernière tentative |

## Formules mathématiques

### Probabilité réelle d'un rang

Pour un rang donné $R$ avec un taux de base $t_R$ :

$$P(R) = \frac{t_R}{\sum_{i \in \text{disponibles}} t_i}$$

### Probabilité d'obtenir un monstre spécifique

Pour un monstre donné dans un rang $R$ contenant $N_R$ monstres :

$$P(\text{monstre}) = P(R) \times \frac{1}{N_R} = \frac{t_R}{N_R \times \sum_{i \in \text{disponibles}} t_i}$$

### Probabilité d'une combinaison monstre + skills

Pour un monstre de rang $R_M$ avec 3 skills de rangs $R_{S1}$, $R_{S2}$, $R_{S3}$ :

$$P(\text{combinaison}) = P(\text{monstre}) \times P(R_{S1}) \times P(R_{S2}) \times P(R_{S3})$$

Note : Les probabilités des skills sont dynamiques et changent après chaque sélection.

## Exemples concrets

### Exemple 1 : Monstre LEGENDARY avec 3 skills COMMON

```
P(LEGENDARY) = 5%
P(monstre spécifique) = 5% / nombre_de_legendaries
P(skill COMMON #1) ≈ 50%
P(skill COMMON #2) ≈ 50% (recalculé après exclusion du premier)
P(skill COMMON #3) ≈ 50% (recalculé après exclusion des deux premiers)
```

### Exemple 2 : Plus de monstres COMMON disponibles

```
Nouveaux taux :
- RARE: 0.3 / 0.5 = 60%
- EPIC: 0.15 / 0.5 = 30%
- LEGENDARY: 0.05 / 0.5 = 10%
```

## Endpoints associés

- `POST /api/invocations/invoke` : Invocation simple (retourne le monstre sans persistance)
- `POST /api/invocations/global-invoke?playerId={id}` : Invocation globale avec Saga
- `POST /api/invocations/replay` : Rejoue les invocations échouées

## Logs et monitoring

Le système log les événements suivants :
- Début et fin d'invocation globale
- Succès de l'ajout du monstre au joueur
- Échecs avec messages d'erreur détaillés
- Déclenchement et résultat des compensations
- Résultats du replay d'invocations

## Voir aussi

- [SAGA_PATTERN.md](SAGA_PATTERN.md) : Détails sur le pattern Saga
- [ARCHITECTURE_INTER_API.md](ARCHITECTURE_INTER_API.md) : Architecture de communication inter-API
