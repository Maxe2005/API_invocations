

# API Invocations

Microservice de gestion des invocations de monstres avec système de probabilités dynamiques et pattern Saga.

## 🎲 Système d'invocation

L'API permet d'invoquer des monstres aléatoires avec des probabilités par rang de rareté :
- **COMMON** (50%) | **RARE** (30%) | **EPIC** (15%) | **LEGENDARY** (5%)

### Fonctionnalités clés
- **Probabilités dynamiques** : Les taux s'ajustent automatiquement selon les données disponibles
- **Pattern Saga** : Garantit la cohérence des données avec compensation automatique en cas d'échec
- **3 compétences par monstre** : Chaque skill a son propre tirage de rareté
- **Replay automatique** : Possibilité de rejouer les invocations échouées

📖 **Documentation complète** : [docs/SYSTEME_INVOCATION.md](docs/SYSTEME_INVOCATION.md)

## Démarer

### Le docker (via l'orchestrateur)

Ce service se lance **exclusivement** via le dépôt orchestrateur [GatchaApi](https://github.com/Maxe2005/GatchaApi) et son `docker-compose.yaml` racine (il n'y a plus de `docker-compose.yml` local dans ce dépôt). Toute la configuration (datasource, URLs des APIs externes, `app.auth.enabled`) est fournie par l'orchestrateur.

```bash
# Depuis la racine du dépôt GatchaApi
make up                        # toute la stack
make restart-api-invocations   # rebuild + restart de ce service uniquement
```

### L'app en local

Le profil `local` pointe sur le PostgreSQL exposé par la stack racine (`localhost:5433`) et sur les APIs exposées en local (`8081`/`8082`/`8083`) :

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

## Vider et relancer BDD par défaut

```bash
# Depuis la racine du dépôt GatchaApi (⚠️ supprime les volumes de toute la stack)
docker compose down -v
docker compose up -d --build
```

## Swagger

```html
http://localhost:8080/swagger-ui/index.html
```

## Visualiser la BDD posgreSQL avec pgAdmin

1. Ouvre ton navigateur sur http://localhost:5050
2. Connecte-toi avec :
    - Email : admin@admin.com
    - Password : admin
3. Une fois connecté, ajoute un serveur :
    - Clic droit "Servers" → "Register" → "Server"
    - Onglet "General" → Name : api_invocations
    - Onglet "Connection" :
        - Host : postgres-invocations
        - Port : 5432
        - Database : api_invocationsdb
        - Username : api_invocations
        - Password : api_invocations

## Documentation

- [Système d'invocation](docs/SYSTEME_INVOCATION.md) - Probabilités et mécanismes de tirage
- [Architecture inter-API](docs/ARCHITECTURE_INTER_API.md) - Communication entre microservices
- [Pattern Saga](docs/SAGA_PATTERN.md) - Gestion des transactions distribuées
- [Stratégie de tests](docs/STRATEGIE_TESTS.md) - Guide de test du projet
