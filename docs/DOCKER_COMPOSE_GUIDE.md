# Exemple de configuration Docker Compose

## Structure recommandée

Voici un exemple de configuration `docker-compose.yml` pour faire communiquer les trois APIs :

```yaml
version: '3.8'

services:
  # Base de données MongoDB partagée (ou une par service si besoin)
  mongodb:
    image: mongo:7.0
    container_name: mongodb
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_DATABASE: api_invocationsdb
    volumes:
      - mongo_data:/data/db
    networks:
      - api_network

  # API Monsters (service externe 1)
  api_monsters:
    image: api_monsters:latest
    container_name: api_monsters
    ports:
      - "8081:8080"
    environment:
      - SPRING_DATA_MONGODB_HOST=mongodb
      - SPRING_DATA_MONGODB_PORT=27017
      - SPRING_DATA_MONGODB_DATABASE=monsters_db
    depends_on:
      - mongodb
    networks:
      - api_network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # API Joueur (service externe 2)
  api_joueur:
    image: api_joueur:latest
    container_name: api_joueur
    ports:
      - "8082:8080"
    environment:
      - SPRING_DATA_MONGODB_HOST=mongodb
      - SPRING_DATA_MONGODB_PORT=27017
      - SPRING_DATA_MONGODB_DATABASE=joueur_db
    depends_on:
      - mongodb
    networks:
      - api_network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # API Invocations (ce service)
  api_invocations:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: api_invocations
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATA_MONGODB_HOST=mongodb
      - SPRING_DATA_MONGODB_PORT=27017
      - SPRING_DATA_MONGODB_DATABASE=Api_invocationsdb
      - EXTERNAL_API_MONSTERS_BASE_URL=http://api_monsters:8080
      - EXTERNAL_API_PLAYER_BASE_URL=http://api_joueur:8080
      - EXTERNAL_API_CONNECTION_TIMEOUT=5000
      - EXTERNAL_API_READ_TIMEOUT=5000
    depends_on:
      - mongodb
      - api_monsters
      - api_joueur
    networks:
      - api_network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  api_network:
    driver: bridge

volumes:
  mongo_data:
```

## Points importants

### 1. Nommage des services
Les noms des services Docker (`api_monsters`, `api_joueur`) **doivent correspondre** aux URLs configurées dans `application.yml` :
```yaml
external:
  api:
    monsters-base-url: http://api_monsters:8080  # ← Nom du service Docker
    player-base-url: http://api_joueur:8080      # ← Nom du service Docker
```

### 2. Réseau Docker
Tous les services doivent être sur le **même réseau** (`api_network`) pour pouvoir communiquer entre eux.

### 3. Variables d'environnement

Pour surcharger la configuration dans Docker, ajoutez ces variables :
```yaml
environment:
  - EXTERNAL_API_MONSTERS_BASE_URL=http://api_monsters:8080
  - EXTERNAL_API_PLAYER_BASE_URL=http://api_joueur:8080
```

Spring Boot les mappera automatiquement à :
```yaml
external:
  api:
    monsters-base-url: ...
    player-base-url: ...
```

### 4. Ordre de démarrage

Utilisez `depends_on` pour s'assurer que :
1. MongoDB démarre en premier
2. Puis les APIs externes (monsters et joueur)
3. Enfin l'API Invocations

### 5. Health checks

Les health checks permettent de vérifier que les services sont prêts avant de les appeler.

## Configuration alternative : application-docker.yml

Si vous préférez ne pas utiliser les variables d'environnement, créez un profil Docker :

```yaml
# src/main/resources/application-docker.yml
spring:
  data:
    mongodb:
      host: mongodb
      port: 27017
      database: Api_invocationsdb

external:
  api:
    monsters-base-url: http://api_monsters:8080
    player-base-url: http://api_joueur:8080
    connection-timeout: 5000
    read-timeout: 5000
```

Puis activez-le dans le docker-compose :
```yaml
api_invocations:
  environment:
    - SPRING_PROFILES_ACTIVE=docker
```

## Démarrage

```bash
# Construire et démarrer tous les services
docker-compose up --build

# Démarrer en mode détaché
docker-compose up -d

# Voir les logs
docker-compose logs -f api_invocations

# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

## Test de communication

### 1. Vérifier que les services sont accessibles

```bash
# Depuis l'hôte
curl http://localhost:8080/actuator/health  # API Invocations
curl http://localhost:8081/actuator/health  # API Monsters
curl http://localhost:8082/actuator/health  # API Joueur

# Depuis le container api_invocations
docker exec api_invocations curl http://api_monsters:8080/actuator/health
docker exec api_invocations curl http://api_joueur:8080/actuator/health
```

### 2. Tester l'invocation globale

```bash
curl -X POST http://localhost:8080/api/invocation/global-invoque/player123
```

### 3. Vérifier les logs

```bash
docker-compose logs -f api_invocations
```

## Troubleshooting

### Erreur : "Connection refused"

**Cause** : Les services ne sont pas sur le même réseau ou les noms ne correspondent pas.

**Solution** :
```bash
# Vérifier les réseaux
docker network ls
docker network inspect <nom_du_reseau>

# Vérifier que les services peuvent se pinger
docker exec api_invocations ping api_monsters
```

### Erreur : "Unknown host"

**Cause** : Le nom du service dans application.yml ne correspond pas au nom dans docker-compose.yml.

**Solution** : Vérifier la cohérence des noms.

### Erreur : "Timeout"

**Cause** : Le service cible n'a pas démarré complètement.

**Solution** : Augmenter les timeouts ou attendre que les health checks soient verts.

```yaml
external:
  api:
    connection-timeout: 10000  # 10 secondes
    read-timeout: 10000        # 10 secondes
```

## Configuration de développement local

Pour développer localement sans Docker Compose :

```yaml
# application-local.yml
external:
  api:
    monsters-base-url: http://localhost:8081
    player-base-url: http://localhost:8082
    connection-timeout: 5000
    read-timeout: 5000
```

Puis démarrez avec le profil `local` :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
