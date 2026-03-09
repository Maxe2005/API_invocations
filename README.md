

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

### Le docker

```bash
docker compose up -d
```

### L'app en local

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

## Vider et relancer BDD par défaut

```bash
docker compose down -v
docker compose up -d --build
```

## Swagger

```
http://localhost:8085/swagger-ui/index.html
```

## Documentation

- [Système d'invocation](docs/SYSTEME_INVOCATION.md) - Probabilités et mécanismes de tirage
- [Architecture inter-API](docs/ARCHITECTURE_INTER_API.md) - Communication entre microservices
- [Pattern Saga](docs/SAGA_PATTERN.md) - Gestion des transactions distribuées
- [Stratégie de tests](docs/STRATEGIE_TESTS.md) - Guide de test du projet
