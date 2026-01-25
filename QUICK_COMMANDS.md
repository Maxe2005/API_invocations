# Commandes Rapides - Tests API Invocations

## 🚀 Démarrage Ultra-Rapide

```bash
# En une ligne: tout testé
mvn clean test && mvn jacoco:report && open target/site/jacoco/index.html

# Ou avec le script
chmod +x run-tests.sh
./run-tests.sh
```

## 📋 Commandes Courantes

### Exécution Basique

```bash
# Tous les tests
mvn clean test

# Pas d'attente, erreurs seulement
mvn test -q

# Avec détails verbeux
mvn test -X

# Stop au premier échec
mvn test -DfailFast=true
```

### Tests Spécifiques

```bash
# Une classe de test
mvn test -Dtest=MonsterServiceTest

# Un test spécifique
mvn test -Dtest=MonsterServiceTest#testCreateMonster

# Plusieurs tests (pattern)
mvn test -Dtest=*Service*

# Tous sauf intégration
mvn test -Dtest=*Service*,*Controller*

# Seulement intégration
mvn test -Dtest=*Integration*

# Saga pattern tests
mvn test -Dtest=SagaPattern*

# Données réalistes
mvn test -Dtest=RealisticData*
```

### Couverture de Code

```bash
# Générer rapport Jacoco
mvn clean test jacoco:report

# Voir rapport (macOS)
open target/site/jacoco/index.html

# Voir rapport (Linux)
xdg-open target/site/jacoco/index.html

# Voir rapport (Windows)
start target\site\jacoco\index.html

# Voir couverture dans le terminal
mvn jacoco:report && grep -A 20 "Coverage"
```

### Exécution Optimisée

```bash
# En parallèle (plus rapide)
mvn test -DparallelTestClasses=true -DthreadsPerCore=2

# Sans compilation (déjà compilé)
mvn test -DskipCompile=true

# Cache accéléré
mvn test -q -o

# Tests et rapport combinés
mvn clean test jacoco:report -q
```

### Nettoyage et Rebuild

```bash
# Tout nettoyer et tester
mvn clean test

# Nettoyer tout sauf target
mvn clean:clean

# Rebuild complet
mvn clean install -DskipTests

# Supprimer cache local
rm -rf ~/.m2/repository/com/imt/
mvn clean install
```

## 🔍 Débogage et Diagnostique

```bash
# Tests avec logs detaillés
mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

# Voir les dépendances
mvn dependency:tree

# Voir quelle version est utilisée
mvn dependency:tree | grep testcontainers

# Tests avec stack trace complet
mvn test -DtrimStackTrace=false

# Voir les propriétés Maven
mvn help:active-profiles

# Voir les plugins exécutés
mvn help:describe -Dplugin=org.apache.maven.plugins:maven-surefire-plugin
```

## 📊 Rapports et Analyses

```bash
# Genérer tous les rapports
mvn clean test jacoco:report surefire:report

# Voir rapports Surefire (résultats tests)
open target/site/surefire-report.html

# Voir rapport Jacoco (couverture)
open target/site/jacoco/index.html

# Exporter couverture en CSV
mvn jacoco:report && ls -la target/site/jacoco/

# Vérifier couverture minimum (optionnel)
mvn jacoco:check
```

## 🏃 Tests Rapides (Unitaires Seulement)

```bash
# Services seulement (10-30 sec)
mvn test -Dtest=*Service*

# Controllers seulement (5-15 sec)
mvn test -Dtest=*Controller*

# Utilitaires et exceptions (< 5 sec)
mvn test -Dtest=*Test -Dtest=!*Integration*
```

## 🐌 Tests Lents (Intégration Complète)

```bash
# Tous les tests d'intégration
mvn test -Dtest=*Integration*

# Saga pattern (complexe)
mvn test -Dtest=SagaPattern*

# Données réalistes (volumes)
mvn test -Dtest=RealisticData*

# Suite complète (3-5 minutes)
mvn clean test -q
```

## 🔄 Développement Itératif (TDD)

```bash
# Boucle TDD rapide
while true; do
  mvn test -Dtest=MonsterServiceTest -q
  echo "Appuyez sur Ctrl+C pour arrêter"
  sleep 2
done

# Ou avec inotifywait (Linux)
inotifywait -r -m src/test/java/ | while read; do
  mvn test -Dtest=MonsterServiceTest -q
done
```

## 🔐 Validation Pre-Commit

```bash
# Vérification complète avant commit
mvn clean test -q && \
mvn jacoco:report -q && \
echo "✓ Tous les tests passent" && \
echo "✓ Rapport de couverture généré"

# Ou en fonction
function test-before-commit() {
  echo "Exécution des tests..."
  mvn clean test -q || return 1
  echo "✓ Tests OK"
  echo "Génération du rapport..."
  mvn jacoco:report -q || return 1
  echo "✓ Rapport OK"
  return 0
}

# Utilisation
test-before-commit && git commit -m "Message"
```

## 📁 Affichage des Résultats

```bash
# Voir les rapports de test générés
ls -la target/surefire-reports/

# Voir le rapport de couverture
ls -la target/site/jacoco/

# Voir le fichier XML de couverture
cat target/site/jacoco-ut/jacoco.xml

# Extraire résumé de couverture
grep "sourcefile" target/site/jacoco-ut/jacoco.xml | wc -l
```

## 🚨 Gestion des Erreurs

```bash
# Tests échouent? Voir les détails
mvn test -DfailIfNoTests=false -Dtest=NomDuTest -X

# Voir le log d'erreur complet
cat target/surefire-reports/com.imt.api_invocations.*.txt

# Parser les erreurs
grep -i "error\|fail" target/surefire-reports/*.txt

# Voir les logs Maven
mvn test -l build.log

# Analyser le log
grep "ERROR\|FAILED" build.log
```

## 🐳 Docker et MongoDB

```bash
# Démarrer MongoDB pour tests
docker run -d -p 27017:27017 --name test-mongo mongo:latest

# Vérifier MongoDB
docker ps | grep mongo

# Logs MongoDB
docker logs test-mongo

# Arrêter MongoDB
docker stop test-mongo

# Nettoyer
docker rm test-mongo

# Exécuter tests avec MongoDB actif
docker run -d -p 27017:27017 mongo:latest && \
mvn clean test && \
docker stop $(docker ps -q)
```

## 📦 Gestion des Dépendances

```bash
# Voir les dépendances de test
mvn dependency:tree -Dscope=test

# Mettre à jour les dépendances
mvn versions:display-dependency-updates

# Nettoyer cache Maven
rm -rf ~/.m2/repository
mvn clean install

# Vérifier les versions
mvn help:describe -Dplugin=maven-surefire-plugin -Dgoal=test
```

## 🎯 Commandes Par Cas d'Usage

### Je viens de commencer
```bash
# 1. Lire les guides
cat TESTING_GUIDE.md | head -50

# 2. Exécuter un test simple
mvn test -Dtest=MonsterServiceTest -q

# 3. Voir le rapport
mvn jacoco:report && open target/site/jacoco/index.html
```

### Je débuggue un test
```bash
# Voir les détails
mvn test -Dtest=NomDuTest -X

# Avec logs
mvn test -Dtest=NomDuTest -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

# Avec stack trace complet
mvn test -Dtest=NomDuTest -DtrimStackTrace=false
```

### Je veux une couverture de 85%
```bash
# Générer rapport
mvn clean test jacoco:report

# Analyser couverture
echo "Ouverture du rapport de couverture..."
open target/site/jacoco/index.html

# Chercher les faibles couvertures
grep "red" target/site/jacoco/index.html || echo "Pas de faible couverture détectée"
```

### Je veux tester en CI/CD
```bash
# Reproduire l'environnement CI
mvn clean test -q -DskipTests=false -Dtest=*Test

# Générer rapports
mvn jacoco:report surefire:report

# Vérifier les fichiers
ls -la target/site/
```

## 💾 Alias Utiles (À ajouter à ~/.bashrc ou ~/.zshrc)

```bash
# Tests rapides
alias mtest='mvn test -q'
alias mtestf='mvn test -q -DfailFast=true'

# Tests complets
alias mtestfull='mvn clean test jacoco:report'

# Couverture
alias mcov='mvn jacoco:report && open target/site/jacoco/index.html'

# Rapports
alias mreport='mvn site:site && open target/site/index.html'

# Nettoyer
alias mclean='mvn clean -q'

# Rebuild
alias mrebuild='mvn clean install -DskipTests -q'

# Tests spécifiques
alias mservice='mvn test -q -Dtest=*Service*'
alias mcontroller='mvn test -q -Dtest=*Controller*'
alias mintegration='mvn test -q -Dtest=*Integration*'
```

Utilisation:
```bash
source ~/.bashrc  # ou ~/.zshrc

# Maintenant vous pouvez faire:
mtest
mtestfull
mcov
mservice
```

## 📈 Performance et Optimisation

```bash
# Profiler les tests (voir lesquels sont lents)
mvn clean test -DforkMode=never -Dtest=*IntegrationTest

# Tests en parallèle
mvn test -DparallelTestClasses=true

# Benchmark simple
time mvn clean test -q

# Voir les détails de performance
mvn test -Dtest=*IntegrationTest -DverboseFailures=true
```

---

**Astuce**: Gardez ce fichier à portée de main pour les commandes courantes!

Pour plus de détails, consultez: TESTING_GUIDE.md
