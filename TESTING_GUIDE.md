# Tests Unitaires et Tests d'Intégration - API Invocations

Ce document décrit la stratégie de test et les tests créés pour l'API Invocations.

## 📋 Vue d'ensemble

La suite de tests comprend:
- **Tests unitaires**: Tests isolés des services et contrôleurs
- **Tests d'intégration**: Tests des endpoints REST complets
- **Mocking**: Utilisation de Mockito pour les dépendances externes

## 📁 Structure des tests

```
src/test/java/com/imt/api_invocations/
├── service/
│   ├── MonsterServiceTest.java
│   ├── SkillsServiceTest.java
│   └── InvocationServiceTest.java
├── controller/
│   ├── InvocationControllerTest.java
│   ├── MonsterControllerTest.java
│   └── SkillsControllerTest.java
└── integration/
    ├── InvocationIntegrationTest.java
    └── MonsterSkillsIntegrationTest.java

src/test/resources/
└── application-test.yml
```

## 🧪 Tests Unitaires

### MonsterServiceTest
Teste la logique métier du service Monster avec Mockito.

**Tests couverts:**
- ✅ Création de monstre
- ✅ Récupération de monstre par ID
- ✅ Récupération de tous les monstres
- ✅ Récupération par rang
- ✅ Mise à jour de monstre
- ✅ Suppression de monstre
- ✅ Sélection aléatoire de monstre par rang
- ✅ Vérification de données disponibles

**Exemple:**
```java
@Test
@DisplayName("Should create a monster successfully")
void testCreateMonster() {
    // Arrange
    MonsterMongoDto monsterDto = new MonsterMongoDto(
        null, Elementary.FIRE, 100, 50, 30, 40, Rank.COMMON
    );
    when(monsterRepository.save(any())).thenReturn("monster123");
    
    // Act
    String result = monsterService.createMonster(monsterDto);
    
    // Assert
    assertEquals("monster123", result);
    verify(monsterRepository, times(1)).save(any());
}
```

### SkillsServiceTest
Teste la logique métier du service Skills.

**Tests couverts:**
- ✅ Création de compétence
- ✅ Récupération de compétence par ID
- ✅ Récupération des compétences d'un monstre
- ✅ Mise à jour de compétence
- ✅ Suppression de compétence
- ✅ Gestion des erreurs (monstre inexistant)

### InvocationServiceTest
Teste la logique d'invocation complexe avec pattern Saga.

**Tests couverts:**
- ✅ Invocation simple d'un monstre
- ✅ Invocation globale avec ajout au joueur
- ✅ Gestion des erreurs et compensation
- ✅ Relecture des invocations tamponnées
- ✅ Gestion des snapshots manquants

**Points clés:**
- Teste le pattern Saga avec compensation
- Vérifie la suppression du monstre en cas d'erreur
- Valide la gestion des buffers d'invocation

## 🎮 Tests des Contrôleurs

### InvocationControllerTest
Tests unitaires du contrôleur Invocation.

**Tests couverts:**
- ✅ GET `/api/invocation/invoque` - Invocation simple
- ✅ POST `/api/invocation/global-invoque/{playerId}` - Invocation globale
- ✅ POST `/api/invocation/recreate` - Relecture des invocations
- ✅ Vérification des statuts HTTP

### MonsterControllerTest
Tests unitaires du contrôleur Monster.

**Tests couverts:**
- ✅ POST `/api/invocation/monsters/create` - Création
- ✅ GET `/api/invocation/monsters/{monsterId}` - Récupération
- ✅ GET `/api/invocation/monsters` - Listing
- ✅ PUT `/api/invocation/monsters/{monsterId}` - Mise à jour
- ✅ DELETE `/api/invocation/monsters/{monsterId}` - Suppression
- ✅ Codes HTTP 404 pour ressources inexistantes

### SkillsControllerTest
Tests unitaires du contrôleur Skills.

**Tests couverts:**
- ✅ POST `/api/invocation/skills` - Création
- ✅ GET `/api/invocation/skills/{skillId}` - Récupération
- ✅ PUT `/api/invocation/skills/{skillId}` - Mise à jour
- ✅ DELETE `/api/invocation/skills/{skillId}` - Suppression
- ✅ Gestion des erreurs de validation

## 🔗 Tests d'Intégration

### InvocationIntegrationTest
Tests complets des endpoints REST.

**Caractéristiques:**
- Utilise `@SpringBootTest` et `@AutoConfigureMockMvc`
- Profile actif: `test`
- Nettoyage des données avant chaque test
- Tests des endpoints réels avec MockMvc

**Tests couverts:**
- ✅ Invocation d'un monstre
- ✅ Création d'un monstre
- ✅ Récupération d'un monstre
- ✅ Création de compétence
- ✅ Erreurs 404
- ✅ Recréation des invocations

### MonsterSkillsIntegrationTest
Tests d'intégration complets du cycle de vie des monstres et compétences.

**Tests couverts:**
- ✅ Cycle de vie complet du monstre (créer → récupérer → mettre à jour → supprimer)
- ✅ Création de monstre avec plusieurs compétences
- ✅ Validation des données
- ✅ Gestion des erreurs d'ID invalides
- ✅ Listing des monstres
- ✅ Suppression de compétences

## 🚀 Exécution des tests

### Exécuter tous les tests
```bash
mvn test
```

### Exécuter les tests d'une classe spécifique
```bash
mvn test -Dtest=MonsterServiceTest
```

### Exécuter un test spécifique
```bash
mvn test -Dtest=MonsterServiceTest#testCreateMonster
```

### Exécuter les tests d'intégration uniquement
```bash
mvn test -Dtest=*IntegrationTest
```

### Exécuter avec couverture de code
```bash
mvn test jacoco:report
```

## 📊 Couverture de code

La couverture cible est:
- **Services**: 85%+
- **Contrôleurs**: 80%+
- **Tests d'intégration**: Points critiques 100%

## 🔧 Configuration des tests

### application-test.yml
Fichier de configuration utilisé pendant les tests:
- MongoDB: `mongodb://localhost:27017/test_api_invocation`
- Logging: DEBUG pour les packages de l'application
- DDL: `create-drop` pour nettoyer après les tests

## 📝 Bonnes pratiques appliquées

1. **Nommage clair**: Les noms de tests expliquent ce qu'ils testent
2. **Pattern AAA**: Arrange → Act → Assert
3. **@DisplayName**: Descriptions lisibles pour les rapports
4. **Isolation**: Chaque test est indépendant
5. **Nettoyage**: `@BeforeEach` pour nettoyer l'état
6. **Mocking**: Mockito pour isoler les dépendances
7. **Assertions complètes**: Vérification de tous les aspects pertinents
8. **Gestion des erreurs**: Tests des cas d'erreur et exceptions

## 🎯 Cas de test prioritaires

| Priorité | Test | Raison |
|----------|------|--------|
| 🔴 Critique | Invocation avec compensation | Pattern Saga critique |
| 🔴 Critique | Création/Suppression de monstre | Opérations principales |
| 🟡 Haute | Validation des données | Sécurité de l'API |
| 🟡 Haute | Gestion des erreurs | Robustesse |
| 🟢 Moyenne | Listing/Récupération | Fonctionnalités standard |

## 🔍 Erreurs communes à tester

- ❌ Création d'une compétence pour un monstre inexistant
- ❌ Création d'un monstre avec des données invalides
- ❌ Accès à des ressources inexistantes (404)
- ❌ Invocation globale avec API externe en erreur
- ❌ Relecture sans données tamponnées

## 📚 Ressources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [Pattern Saga](https://microservices.io/patterns/data/saga.html)

## ✅ Checklist d'exécution

Avant de commiter le code:
- [ ] Tous les tests passent: `mvn test`
- [ ] Pas d'erreurs de compilation
- [ ] Couverture de code vérifiée
- [ ] Tests d'intégration exécutés avec MongoDB disponible
- [ ] Code formaté et nettoyé

## 📞 Dépannage

### Les tests MongoDB échouent
**Solution**: Assurez-vous que MongoDB est en cours d'exécution:
```bash
# Avec Docker
docker-compose up -d mongodb
```

### Les tests sont lents
**Solution**: Exécutez les tests unitaires et d'intégration séparément
```bash
mvn test -Dtest=*Service*  # Unitaires (rapides)
mvn test -Dtest=*IntegrationTest  # Intégration (plus lents)
```

### Les tests échouent de manière intermittente
**Cause probable**: Problèmes de timing ou d'ordre des tests
**Solution**: Utilisez `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` et `@Order`

## 🔄 Cycle de développement

1. Écrire le test (TDD)
2. Exécuter et voir échouer
3. Implémenter la fonctionnalité
4. Exécuter et voir passer
5. Refactoriser
6. Valider tous les tests

---

**Dernière mise à jour**: 23 Janvier 2026
