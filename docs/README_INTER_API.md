# 🎯 Architecture de Communication Inter-API - Guide Rapide

## 📦 Ce qui a été créé

Vous disposez maintenant d'une **architecture complète et professionnelle** pour la communication entre microservices, respectant les principes **SOLID** et **DRY**, avec gestion des transactions distribuées via le **Pattern Saga**.

## 🚀 Démarrage rapide

### 1. Configuration

Modifiez `application.yml` si nécessaire :
```yaml
external:
  api:
    monsters-base-url: http://api_monsters:8080
    player-base-url: http://api_joueur:8080
```

### 2. Démarrage avec Docker

```bash
cd docker-dev-env
docker compose up
```

### 3. Test de l'API

```bash
# Invoquer un monstre et l'ajouter au joueur
curl -X POST http://localhost:8080/api/invocation/global-invoque/player123
```

## 📁 Structure des fichiers

```
src/main/java/com/imt/api_invocations/
├── client/                           # Clients pour APIs externes
│   ├── MonstersApiClient.java       # Communication avec API Monsters
│   ├── PlayerApiClient.java         # Communication avec API Joueur
│   ├── ExternalApiClient.java       # Interface commune
│   └── dto/                          # DTOs de communication
│       ├── CreateMonsterRequest.java
│       ├── CreateMonsterResponse.java
│       ├── AddMonsterRequest.java
│       └── AddMonsterResponse.java
├── config/
│   ├── ExternalApiProperties.java   # Configuration centralisée
│   └── RestClientConfig.java        # Configuration RestTemplate
├── exception/
│   └── ExternalApiException.java    # Exception inter-API
└── service/
    └── InvocationService.java       # ✨ globalInvoke() implémenté ici

Documentation/
├── IMPLEMENTATION_SUMMARY.md        # 📋 Résumé complet
├── ARCHITECTURE_INTER_API.md        # 🏗️ Architecture détaillée
├── SAGA_PATTERN.md                  # 🔄 Explication du pattern Saga
├── SEQUENCE_DIAGRAM.md              # 📊 Diagrammes de séquence
└── DOCKER_COMPOSE_GUIDE.md          # 🐳 Configuration Docker
```

## 🔄 Fonctionnement du Pattern Saga

### Flux normal
```
1. Invoquer un monstre localement
2. Créer le monstre dans API Monsters → monsterId
3. Ajouter le monstre au joueur
✅ Succès !
```

### Flux avec compensation
```
1. Invoquer un monstre localement ✅
2. Créer le monstre dans API Monsters ✅ → monsterId
3. Ajouter le monstre au joueur ❌ ÉCHEC
   └─→ COMPENSATION : Supprimer le monstre (monsterId)
```

## 📚 Documentation disponible

1. **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Vue d'ensemble complète
2. **[ARCHITECTURE_INTER_API.md](ARCHITECTURE_INTER_API.md)** - Principes SOLID et DRY
3. **[SAGA_PATTERN.md](SAGA_PATTERN.md)** - Pattern Saga en détail
4. **[SEQUENCE_DIAGRAM.md](SEQUENCE_DIAGRAM.md)** - Diagrammes visuels
5. **[DOCKER_COMPOSE_GUIDE.md](DOCKER_COMPOSE_GUIDE.md)** - Configuration Docker

## 🧪 Tests

Exécuter les tests unitaires :
```bash
mvn test
```

Tests couverts :
- ✅ Invocation réussie
- ✅ Compensation en cas d'échec de l'API Joueur
- ✅ Pas de compensation si l'API Monsters échoue

## ✨ Points forts

| Aspect | Implémentation |
|--------|---------------|
| 🎯 **SOLID** | Chaque classe a une responsabilité unique |
| 🔄 **DRY** | Configuration et code réutilisables |
| 🛡️ **Résilience** | Compensation automatique en cas d'échec |
| 📊 **Traçabilité** | Logs détaillés à chaque étape |
| 🧪 **Testabilité** | Architecture mockable facilement |
| 📖 **Documentation** | Documentation complète et exemples |

## 🔍 Vérifications

### Les APIs externes doivent exposer :

**API Monsters** :
- `POST /api/monsters/create` → Retourne `{monsterId: "..."}`
- `DELETE /api/monsters/{id}` → Supprime le monstre

**API Joueur** :
- `POST /api/joueur/add_monster` → Reçoit `{playerId, monsterId}`

### Noms des services Docker

Dans `docker-compose.yml` :
```yaml
services:
  api_monsters:    # ← Ce nom est utilisé dans application.yml
  api_joueur:      # ← Ce nom est utilisé dans application.yml
  api_invocations: # ← Ce service
```

## 🐛 Troubleshooting

| Erreur | Solution |
|--------|----------|
| `Connection refused` | Vérifier que les services sont sur le même réseau Docker |
| `Unknown host` | Vérifier les noms dans application.yml et docker-compose.yml |
| `Timeout` | Augmenter `connection-timeout` et `read-timeout` |
| `502 Bad Gateway` | Vérifier les logs des APIs externes |

## 📞 Endpoints disponibles

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/api/invocation/invoque` | Invocation simple (existant) |
| `POST` | `/api/invocation/global-invoque/{playerId}` | Invocation globale (nouveau) |

## 🔮 Évolutions futures possibles

- [ ] **Circuit Breaker** avec Resilience4j
- [ ] **Retry Policy** avec exponential backoff
- [ ] **Distributed Tracing** avec Sleuth/Zipkin
- [ ] **Event Sourcing** avec Kafka
- [ ] **Saga Log Table** pour persistance
- [ ] **Async Processing** avec queues

## 🎓 Principes appliqués

### SOLID
- ✅ **S**ingle Responsibility : Chaque client gère une seule API
- ✅ **O**pen/Closed : Extensible sans modification
- ✅ **L**iskov Substitution : Clients mockables
- ✅ **I**nterface Segregation : Interfaces ciblées
- ✅ **D**ependency Inversion : Injection de dépendances

### DRY (Don't Repeat Yourself)
- ✅ Configuration centralisée
- ✅ Client HTTP réutilisable
- ✅ Gestion d'erreurs centralisée

### Saga Pattern
- ✅ Transactions distribuées
- ✅ Compensation automatique
- ✅ Cohérence éventuelle

---

## 🎉 Prêt à l'emploi !

Votre système de communication inter-API est maintenant **opérationnel** et **prêt pour la production** !

Pour plus de détails, consultez la documentation complète dans les fichiers `.md` mentionnés ci-dessus.

**Bon développement ! 🚀**
