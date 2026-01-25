# Checklist de Mise en Place des Tests

## ✅ Avant de Commencer

- [ ] Lire le fichier `TEST_SUMMARY.md`
- [ ] Lire le fichier `TESTING_GUIDE.md`
- [ ] Vérifier que Java 21 est installé: `java -version`
- [ ] Vérifier que Maven est installé: `mvn --version`
- [ ] Se placer dans le répertoire racine du projet

## ✅ Installation des Dépendances (Optionnel mais Recommandé)

Suivez les instructions dans `TEST_DEPENDENCIES.md` pour ajouter:

- [ ] Testcontainers (pour MongoDB automatisé)
- [ ] AssertJ (pour assertions fluides)
- [ ] Jacoco (pour la couverture de code)
- [ ] Mettre à jour le `pom.xml`
- [ ] Exécuter: `mvn clean install`

## ✅ Configuration de MongoDB

Choisir l'une des options:

**Option 1: MongoDB Local (recommandé pour développement)**
- [ ] Installer MongoDB localement ou via Docker
- [ ] Vérifier que MongoDB écoute sur `localhost:27017`
- [ ] Commande Docker: `docker run -d -p 27017:27017 mongo:latest`

**Option 2: Testcontainers (recommandé pour CI/CD)**
- [ ] Installer Docker
- [ ] Ajouter la dépendance Testcontainers au pom.xml
- [ ] Exécuter les tests (MongoDB sera créé automatiquement)

**Option 3: MongoDB Atlas (cloud)**
- [ ] Créer un cluster sur https://www.mongodb.com/cloud/atlas
- [ ] Configurer l'URI dans `application-test.yml`

## ✅ Exécution Initiale des Tests

```bash
# Option 1: Script automatique
chmod +x run-tests.sh
./run-tests.sh

# Option 2: Commandes Maven
mvn clean test

# Option 3: Tests spécifiques
mvn test -Dtest=MonsterServiceTest
```

## ✅ Validation des Résultats

- [ ] Tous les tests passent (BUILD SUCCESS)
- [ ] Pas de warnings critiques
- [ ] Logs ne contiennent pas d'erreurs
- [ ] Rapport Jacoco généré (couverture)

## ✅ Configuration IDE (VS Code ou IntelliJ)

### VS Code
- [ ] Installer extension "Test Runner for Java"
- [ ] Installer extension "Extension Pack for Java"
- [ ] Clic droit sur un test > "Run Test"

### IntelliJ IDEA
- [ ] Tests visibles dans l'arborescence
- [ ] Clic droit > "Run 'TestName'"
- [ ] Affichage du rapport de couverture intégré

## ✅ Rapport de Couverture

```bash
# Générer le rapport
mvn clean test jacoco:report

# Ouvrir le rapport
open target/site/jacoco/index.html

# Objectifs de couverture
# - Services: 85%+
# - Contrôleurs: 80%+
# - Global: 75%+
```

## ✅ Tests en Continu (Watch Mode)

```bash
# Terminal 1: Compilation en continu
mvn clean compile -DskipTests -q --watch

# Terminal 2: Tests en continu
mvn test -f pom.xml -q --watch
```

## ✅ Exécution par Catégorie

```bash
# Seulement les tests unitaires (rapide - ~10-30 sec)
mvn test -Dtest=*Service*Test

# Seulement les tests d'intégration (plus lent - ~1-2 min)
mvn test -Dtest=*IntegrationTest

# Tests Saga pattern
mvn test -Dtest=SagaPattern*

# Tous les tests sauf intégration
mvn test -DexcludedGroups=integration
```

## ✅ Développement Guidé par les Tests (TDD)

1. [ ] Écrire un test qui échoue
2. [ ] Exécuter le test: `mvn test -Dtest=NomDuTest`
3. [ ] Voir le test échouer (RED)
4. [ ] Implémenter la fonctionnalité
5. [ ] Exécuter le test à nouveau
6. [ ] Voir le test passer (GREEN)
7. [ ] Refactoriser le code
8. [ ] Exécuter tous les tests

## ✅ Dépannage

### Les tests mongoDB échouent
```bash
# Vérifier que MongoDB est en cours d'exécution
docker ps | grep mongo

# Redémarrer MongoDB
docker restart <container-id>

# Ou utiliser Testcontainers (automatique)
mvn test -Dtest=*IntegrationTest
```

### Les tests sont lents
```bash
# Exécuter seulement les tests unitaires
mvn test -Dtest=*Service*

# Exécuter en parallèle
mvn test -DparallelTestClasses=true
```

### Erreurs de dépendances
```bash
# Nettoyer et réinstaller
mvn clean install -U

# Vérifier l'arbre des dépendances
mvn dependency:tree
```

## ✅ Intégration Continue (GitHub Actions)

### Fichier à créer: `.github/workflows/test.yml`
- [ ] Créer le répertoire `.github/workflows/`
- [ ] Ajouter les actions Maven
- [ ] Configurer le cache Maven
- [ ] Ajouter le rapport de couverture

## ✅ Qualité du Code

```bash
# SonarQube (optionnel)
mvn clean verify sonar:sonar

# Checkstyle
mvn checkstyle:check

# SpotBugs
mvn spotbugs:check
```

## ✅ Documentation

- [ ] Lire: TESTING_GUIDE.md
- [ ] Lire: TEST_DEPENDENCIES.md
- [ ] Lire: TEST_SUMMARY.md
- [ ] Mettre à jour: README.md (ajouter section Tests)

## ✅ Avant de Commiter

```bash
# Vérification finale
mvn clean test
mvn jacoco:report

# Vérifier:
# - ✓ Tous les tests passent
# - ✓ Couverture > 75%
# - ✓ Pas d'avertissements
# - ✓ Code formaté
```

## ✅ Bonnes Pratiques

- [ ] Un test par cas métier
- [ ] Noms de tests explicites (@DisplayName)
- [ ] Pattern AAA: Arrange, Act, Assert
- [ ] Pas de test interdépendant
- [ ] Cleanup avec @BeforeEach
- [ ] Mocking des dépendances externes
- [ ] Tests rapides (< 1 sec pour unitaires)

## ✅ Prochaines Étapes

### Court terme
1. [ ] Exécuter tous les tests: `mvn test`
2. [ ] Générer le rapport de couverture
3. [ ] Consulter le rapport Jacoco
4. [ ] Valider la couverture cible (75%+)

### Moyen terme
1. [ ] Ajouter les tests pour les mappers DTO
2. [ ] Améliorer la couverture à 85%+
3. [ ] Ajouter des tests de performance
4. [ ] Configurer le CI/CD

### Long terme
1. [ ] Maintenance régulière des tests
2. [ ] Amélioration continue de la couverture
3. [ ] Intégration avec SonarQube
4. [ ] Dashboard de qualité

## 📞 Support

Si vous avez des questions:

1. Consultez `TESTING_GUIDE.md` - Guide complet
2. Consultez `TEST_SUMMARY.md` - Vue d'ensemble
3. Consultez les tests existants comme exemples
4. Vérifiez la documentation Spring Boot Testing

---

**Version**: 1.0
**Créé**: 23 Janvier 2026
**Status**: ✅ Prêt à utiliser
