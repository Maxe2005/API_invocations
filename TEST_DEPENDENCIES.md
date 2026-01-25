# Dépendances de test recommandées pour pom.xml

Pour améliorer la couverture de test et utiliser Testcontainers, ajoutez les dépendances suivantes au fichier `pom.xml`:

## Configuration recommandée

```xml
<!-- Dans la section <properties> -->
<properties>
    <java.version>21</java.version>
    <testcontainers.version>1.19.7</testcontainers.version>
</properties>

<!-- Dans la section <dependencies>, ajouter dans la section test: -->

<!-- Testcontainers pour MongoDB -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>

<!-- AssertJ pour assertions fluides -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.1</version>
    <scope>test</scope>
</dependency>

<!-- RestAssured pour tests REST -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>

<!-- JSON Path pour assertions JSON -->
<dependency>
    <groupId>com.jayway.jsonpath</groupId>
    <artifactId>json-path</artifactId>
    <version>2.8.1</version>
    <scope>test</scope>
</dependency>

<!-- Faaker pour générer des données de test -->
<dependency>
    <groupId>com.github.javafaker</groupId>
    <artifactId>javafaker</artifactId>
    <version>1.0.2</version>
    <scope>test</scope>
</dependency>
```

## Plugins Maven recommandés

Ajoutez les plugins suivants pour améliorer la qualité des tests:

```xml
<!-- Dans la section <build><plugins> -->

<!-- Jacoco pour la couverture de code -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<!-- Surefire pour l'exécution des tests -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
    </configuration>
</plugin>

<!-- Failsafe pour les tests d'intégration -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.1.2</version>
</plugin>
```

## Commandes d'exécution avec ces dépendances

```bash
# Exécuter tous les tests
mvn clean test

# Exécuter les tests avec rapport Jacoco
mvn clean test jacoco:report

# Voir le rapport de couverture
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux

# Exécuter seulement les tests d'intégration
mvn clean verify -DskipTests=false

# Exécuter avec logs détaillés
mvn clean test -X
```

## Configuration de GitHub Actions (optionnel)

Si vous utilisez GitHub, créez `.github/workflows/test.yml`:

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mongodb:
        image: mongo:latest
        options: >-
          --health-cmd "mongosh --eval \"db.adminCommand('ping')\"" 
          --health-interval 10s 
          --health-timeout 5s 
          --health-retries 5
        ports:
          - 27017:27017

    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Run tests
        run: mvn clean test
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml
```

## Installation des dépendances

```bash
# Copier les dépendances dans le pom.xml
# Puis exécuter:
mvn clean install

# Vérifier que les dépendances sont bien installées
mvn dependency:tree
```

## Vérification

Après avoir ajouté ces dépendances, vérifiez que tout fonctionne:

```bash
# Compiler le projet
mvn clean compile

# Exécuter les tests
mvn test

# Générer le rapport de couverture
mvn jacoco:report

# Voir les résultats
# - Tests: target/surefire-reports/
# - Couverture: target/site/jacoco/
```

---

**Note**: Toutes les dépendances de test doivent avoir `<scope>test</scope>` pour ne pas être incluses en production.
