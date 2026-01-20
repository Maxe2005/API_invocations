# Commandes Utiles - API Invocations

## 🛠️ Développement

### Compilation
```bash
# Compiler le projet
mvn clean compile

# Compiler et packager (skip tests)
mvn clean package -DskipTests

# Compiler avec tests
mvn clean package
```

### Tests
```bash
# Exécuter tous les tests
mvn test

# Exécuter un test spécifique
mvn test -Dtest=InvocationServiceTest

# Exécuter avec couverture de code
mvn test jacoco:report

# Voir le rapport de couverture
open target/site/jacoco/index.html
```

### Démarrage local
```bash
# Démarrer avec profil local
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Démarrer avec profil docker
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Démarrer avec debug activé
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## 🐳 Docker

### Build et démarrage
```bash
# Build de l'image Docker
docker build -t api_invocations:latest .

# Démarrer avec docker-compose
cd docker-dev-env
docker compose up

# Démarrer en mode détaché
docker compose up -d

# Build et démarrage
docker compose up --build

# Rebuild forcé
docker compose build --no-cache
docker compose up
```

### Gestion des conteneurs
```bash
# Voir les conteneurs actifs
docker compose ps

# Voir les logs
docker compose logs -f api_invocations

# Voir les logs de tous les services
docker compose logs -f

# Arrêter les services
docker compose down

# Arrêter et supprimer les volumes
docker compose down -v

# Redémarrer un service spécifique
docker compose restart api_invocations
```

### Inspection
```bash
# Entrer dans le conteneur
docker exec -it api_invocations bash

# Vérifier les variables d'environnement
docker exec api_invocations env | grep EXTERNAL

# Tester la connectivité réseau
docker exec api_invocations ping api_monsters
docker exec api_invocations curl http://api_monsters:8080/actuator/health
docker exec api_invocations curl http://api_joueur:8080/actuator/health

# Inspecter le réseau
docker network ls
docker network inspect docker-dev-env_api_network
```

## 🧪 Tests d'API

### Endpoints de santé
```bash
# API Invocations
curl http://localhost:8080/actuator/health

# API Monsters
curl http://localhost:8081/actuator/health

# API Joueur
curl http://localhost:8082/actuator/health
```

### Invocation simple
```bash
# GET - Invocation simple
curl http://localhost:8080/api/invocation/invoque

# Avec jq pour formater le JSON
curl http://localhost:8080/api/invocation/invoque | jq
```

### Invocation globale (Pattern Saga)
```bash
# POST - Invocation globale
curl -X POST http://localhost:8080/api/invocation/global-invoque/player123

# Avec headers et verbose
curl -X POST \
  -H "Content-Type: application/json" \
  -v \
  http://localhost:8080/api/invocation/global-invoque/player123

# Avec jq pour formater
curl -X POST http://localhost:8080/api/invocation/global-invoque/player123 | jq

# Tester différents joueurs
curl -X POST http://localhost:8080/api/invocation/global-invoque/player456
curl -X POST http://localhost:8080/api/invocation/global-invoque/player789
```

### Tests avec différents scénarios
```bash
# Scénario 1 : Succès complet
curl -X POST http://localhost:8080/api/invocation/global-invoque/player-valid

# Scénario 2 : Simuler une erreur (si API Monsters down)
docker compose stop api_monsters
curl -X POST http://localhost:8080/api/invocation/global-invoque/player123
docker compose start api_monsters

# Scénario 3 : Simuler une erreur (si API Joueur down)
docker compose stop api_joueur
curl -X POST http://localhost:8080/api/invocation/global-invoque/player123
docker compose start api_joueur
```

## 📊 Monitoring et Logs

### Logs en temps réel
```bash
# Tous les services
docker compose logs -f

# Service spécifique
docker compose logs -f api_invocations

# Filtrer par niveau
docker compose logs -f api_invocations | grep ERROR
docker compose logs -f api_invocations | grep WARN
docker compose logs -f api_invocations | grep "Compensation"

# Dernières N lignes
docker compose logs --tail=100 api_invocations
```

### Métriques
```bash
# Métriques Actuator
curl http://localhost:8080/actuator/metrics

# Métriques spécifiques
curl http://localhost:8080/actuator/metrics/http.server.requests
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## 🗄️ MongoDB

### Connexion à MongoDB
```bash
# Via Docker
docker exec -it mongodb mongosh

# Depuis le host (si port exposé)
mongosh mongodb://localhost:27017/Api_invocationsdb
```

### Commandes MongoDB
```javascript
// Lister les bases de données
show dbs

// Utiliser la base
use Api_invocationsdb

// Lister les collections
show collections

// Compter les documents
db.monsters.countDocuments()
db.skills.countDocuments()

// Voir les derniers monstres invoqués
db.monsters.find().sort({_id: -1}).limit(5)

// Supprimer tous les documents (attention !)
db.monsters.deleteMany({})
```

## 🔧 Utilitaires

### Nettoyage
```bash
# Supprimer les images non utilisées
docker image prune

# Supprimer tous les volumes non utilisés
docker volume prune

# Nettoyage complet Docker
docker system prune -a --volumes

# Nettoyer Maven
mvn clean

# Nettoyer Maven et Docker
mvn clean && docker compose down -v
```

### Variables d'environnement
```bash
# Afficher la configuration actuelle
docker exec api_invocations env | grep -E "EXTERNAL|SPRING"

# Modifier une variable (redémarrage requis)
# Éditer docker-compose.yml puis:
docker compose down
docker compose up -d
```

## 📝 Scripts utiles

### Script de test complet
```bash
#!/bin/bash
# test-complete.sh

echo "🚀 Démarrage des services..."
docker compose up -d

echo "⏳ Attente du démarrage (30s)..."
sleep 30

echo "✅ Test de santé..."
curl -f http://localhost:8080/actuator/health || exit 1
curl -f http://localhost:8081/actuator/health || exit 1
curl -f http://localhost:8082/actuator/health || exit 1

echo "🎮 Test d'invocation globale..."
curl -X POST http://localhost:8080/api/invocation/global-invoque/player-test

echo "📊 Logs..."
docker compose logs --tail=50 api_invocations

echo "✨ Tests terminés !"
```

### Script de redémarrage propre
```bash
#!/bin/bash
# restart-clean.sh

echo "🛑 Arrêt des services..."
docker compose down -v

echo "🧹 Nettoyage..."
docker system prune -f

echo "🏗️ Rebuild..."
docker compose build --no-cache

echo "🚀 Démarrage..."
docker compose up -d

echo "⏳ Attente du démarrage (30s)..."
sleep 30

echo "✅ Vérification..."
curl http://localhost:8080/actuator/health

echo "✨ Redémarrage terminé !"
```

## 🔍 Debugging

### Debug d'une API externe
```bash
# Depuis le conteneur api_invocations
docker exec -it api_invocations bash

# Tester la résolution DNS
nslookup api_monsters
nslookup api_joueur

# Tester la connectivité
curl -v http://api_monsters:8080/actuator/health
curl -v http://api_joueur:8080/actuator/health

# Vérifier les routes
curl -v http://api_monsters:8080/api/monsters/create
```

### Activer les logs détaillés
Ajouter dans `application.yml` :
```yaml
logging:
  level:
    com.imt.api_invocations.client: DEBUG
    org.springframework.web.client: DEBUG
```

Puis redémarrer :
```bash
docker compose restart api_invocations
```

## 📚 Commandes de documentation

### Générer la JavaDoc
```bash
mvn javadoc:javadoc

# Ouvrir la JavaDoc
open target/site/apidocs/index.html
```

### Générer le rapport de dépendances
```bash
mvn dependency:tree

# Avec fichier de sortie
mvn dependency:tree > dependencies.txt
```

---

**Astuce** : Ajoutez ces scripts dans un dossier `scripts/` pour faciliter leur utilisation !
