# Guide de Configuration Modulaire de la Base de Données

## Vue d'ensemble
La nouvelle architecture permet de charger et maintenir les données des monstres et compétences via des **fichiers JSON individuels par monstre**, rendant le système très modulaire et facile à mettre à jour et scalable.

## Structure mise en place

### 1. **Fichiers JSON individuels**
- **Localisation**: `src/main/resources/monsters/`
- **Nommage**: `{monstre}.json` (ex: `aquaspherе.json`, `zephirion.json`)
- **Format**: JSON structuré avec le monstre et ses compétences
- **Avantages**: 
  - Un fichier par monstre = lisibilité maximale
  - Git-friendly (diffs et merges minimisés)
  - Travail parallèle sur plusieurs monstres

**Exemple de structure**:
```
src/main/resources/
└── monsters/
    ├── aquaspherе.json
    ├── zephirion.json
    ├── nouveau_monstre.json
    ├── ...
    └── .TEMPLATE_monster.json (template de référence)
```

### 2. **DTOs de Seeding** (dans `config/seeding/`)
- `MonsterSeedDto.java`: Représentation d'un monstre depuis JSON
- `StatsSeedDto.java`: Les statistiques du monstre
- `SkillSeedDto.java`: Les compétences
- `RatioSeedDto.java`: Les ratios de dégâts

### 3. **Service de Chargement**
- `MonsterSeedingService.java`: 
  - Scanne le répertoire `monsters/` et charge tous les fichiers JSON
  - Supporte les patterns (pour charger des sous-ensembles de monstres)
  - Gestion d'erreurs robuste et logging
  - Peut charger un monstre individuel

### 4. **DatabaseSeeder mis à jour**
- Utilise le service pour charger tous les monstres
- Convertit les DTOs en entités de base de données
- Sauvegarde monstres et compétences en transaction

## Cas d'utilisation

### **Ajouter un nouveau monstre (approche recommandée)**
1. Copier le template `.TEMPLATE_monster.json`
2. Créer un nouveau fichier `{nom_monstre}.json`
3. Remplir les données du monstre
4. Relancer l'application

**Exemple**:
```bash
cp src/main/resources/monsters/.TEMPLATE_monster.json \
   src/main/resources/monsters/mon_nouveau_monstre.json
```

Puis éditer `mon_nouveau_monstre.json`:
```json
{
  "nom": "Phénix Lunaire",
  "element": "FIRE",
  "rang": "EPIC",
  "stats": {
    "hp": 600.0,
    "atk": 120.0,
    "def": 60.0,
    "vit": 100.0
  },
  "description_carte": "Un phénix majestueux aux ailes d'argent lunaire...",
  "description_visuelle": "Oiseau de feu géant avec des reflets lunaires...",
  "skills": [
    {
      "name": "Aile de Lune",
      "description": "Attaque ailée brillante comme la lune",
      "damage": 200.0,
      "ratio": {
        "stat": "ATK",
        "percent": 1.0
      },
      "cooldown": 0.0,
      "lvlMax": 5.0,
      "rank": "EPIC"
    }
  ]
}
```

### **Modifier un monstre existant**
1. Éditer le fichier JSON du monstre
2. Supprimer la base de données ou sa collection
3. Relancer l'application

### **Charger un monstre spécifique** (programmativement)
```java
MonsterSeedDto monster = monsterSeedingService.loadMonster("mon_monstre.json");
```

### **Charger tous les monstres**
```java
List<MonsterSeedDto> monsters = monsterSeedingService.loadAllMonsters();
```

### **Charger avec un pattern spécifique**
```java
// Charger uniquement les monstres "rares" (fichiers nommés rare_*.json)
List<MonsterSeedDto> rareMonstes = monsterSeedingService.loadMonstersFromPattern("classpath:monsters/rare_*.json");
```

## Énumérations acceptées

### **Elementary** (elements)
- `FIRE` (Feu)
- `WATER` (Eau)
- `WIND` (Vent)
- `EARTH` (Terre)
- (Autres selon votre enum)

### **Rank** (rangs)
- `COMMON` (Commun)
- `RARE` (Rare)
- `EPIC` (Épique)
- `LEGENDARY` (Légendaire)

### **Stat** (statistiques pour les ratios)
- `HP` (Points de vie)
- `ATK` (Attaque)
- `DEF` (Défense)
- `VIT` (Vitesse)

## Avantages de cette approche

| Aspect | Ancien (Code) | Nouveau (JSON) | Un fichier par monstre |
|--------|-------|-------|--------|
| **Modification données** | Recompilation | Simple édition JSON | ✅ Simple édition JSON |
| **Maintenance** | Code/données mélangés | Séparation claire | ✅ Très claire |
| **Scalabilité** | Difficile (100+ monstres en code) | Facile | ✅ Excellente pour 100+ |
| **Git** | Diffs volumineux | Meilleur | ✅ Optimal (un fichier = un monstre) |
| **Travail collaboratif** | Conflits fréquents | Moins de conflits | ✅ Conflits rares |
| **Versionning** | Données couplées au code | Données indépendantes | ✅ Données vraiment indépendantes |
| **Lisibilité** | Code très long | Fichiers moyens | ✅ Fichiers compacts |

## Architecture du chargement

```
DatabaseSeeder.run()
    ↓
MonsterSeedingService.loadAllMonsters()
    ↓
PathMatchingResourcePatternResolver
    ↓
Scanne: classpath:monsters/*.json
    ↓
Pour chaque fichier trouvé:
    └─ ObjectMapper.readValue() → MonsterSeedDto
    ↓
List<MonsterSeedDto> retournée
    ↓
seedMonsters() convertit en entités DB
    ↓
Sauvegarde dans MongoDB
```

## Bonnes pratiques

1. **Nommage cohérent**: utiliser des noms descriptifs (ex: `phénix_lunaire.json`)
2. **Validation JSON**: valider la syntaxe JSON avant le commit
3. **Template de référence**: consulter `.TEMPLATE_monster.json` pour la structure
4. **Logs**: vérifier les logs au démarrage pour confirmer le chargement
5. **Pas de monstres dans le code**: tout doit être dans JSON

## Améliorations futures possibles

1. **Validation JSON au démarrage**
   ```java
   @Value("${app.seeding.validate:true}")
   private Boolean validateJson;
   ```

2. **Support de propriétés Spring**
   ```java
   @Value("${app.seeding.monsters-dir:monsters}")
   private String monstersDir;
   ```

3. **Interface Web pour l'administration des monstres**
   - Créer/modifier/supprimer des monstres
   - Exporter/importer en JSON

4. **Support des migrations de schéma**
   - Version dans le JSON pour gérer les changements de structure

5. **Chargement dynamique à l'exécution**
   - Recharger les données sans redémarrer
   - Utile pour les mises à jour de balance

6. **Profils d'environnement**
   ```
   monsters/
   ├── base/
   ├── dev/
   ├── prod/
   ```

---

Cette architecture avec **un fichier JSON par monstre** offre **flexibilité maximale**, **maintenabilité** et **scalabilité** tout en restant **simple à utiliser** pour ajouter des centaines de monstres!

