# Stratégie de Tests - API Invocations

## Vue d'ensemble

Cette stratégie vise à atteindre une couverture optimale et fonctionnelle de l'application selon la pyramide de tests :
- **70% Tests Unitaires** : Logique métier isolée
- **20% Tests d'Intégration** : Composants interconnectés
- **10% Tests E2E** : Workflows complets

---

## 🎯 Objectifs de Couverture

| Catégorie | Couverture Cible | Priorité |
|-----------|-----------------|----------|
| Services | 90% | Haute |
| Controllers | 85% | Haute |
| Repositories | 80% | Moyenne |
| Clients API | 90% | Haute |
| Mappers | 100% | Moyenne |
| Utils | 95% | Moyenne |
| Config | 70% | Basse |

---

## 📚 Dépendances de Test à Ajouter

```xml
<!-- Tests unitaires -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Tests d'intégration -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>

<!-- Mock des API externes -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8-standalone</artifactId>
    <version>3.0.1</version>
    <scope>test</scope>
</dependency>

<!-- Tests REST -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- AssertJ pour des assertions fluides -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 🔬 PARTIE 1 : Tests Unitaires

### 1.1 Services

#### **1.1.1 InvocationService** (Priorité CRITIQUE)

**Fichier** : `InvocationServiceTest.java`

##### Méthode `invoke()`
- ✅ Test : Invocation retourne un monstre avec skills aléatoires
- ✅ Test : Le rank du monstre est correctement sélectionné selon les probabilités
- ✅ Test : Les 3 skills sont bien récupérés
- ✅ Test : Le mapping vers GlobalMonsterDto est correct

##### Méthode `globalInvoke(playerId)`
- ✅ Test : Succès complet du workflow Saga
  - Vérifie création du buffer avec status PENDING
  - Vérifie appel à MonstersApiClient avec bon request
  - Vérifie status passe à MONSTER_CREATED
  - Vérifie appel à PlayerApiClient avec monsterId
  - Vérifie status passe à COMPLETED
- ✅ Test : Échec lors de la création du monstre dans l'API externe
  - Vérifie que l'exception est propagée
  - Vérifie que le buffer est marqué FAILED_MONSTER_CREATION
  - Vérifie qu'aucune compensation n'est effectuée
- ✅ Test : Échec lors de l'ajout au joueur
  - Vérifie appel de compensation (deleteMonster)
  - Vérifie status FAILED_PLAYER_ADD
  - Vérifie que l'exception est propagée
- ✅ Test : ExternalApiException gérée correctement
- ✅ Test : Retry increment sur échec

##### Méthode `replayBufferedInvocations()`
- ✅ Test : Replay des invocations en PENDING
- ✅ Test : Replay des invocations en FAILED_PLAYER_ADD avec compensation
- ✅ Test : Invocations COMPLETED ignorées
- ✅ Test : Gestion des échecs successifs (max attempts)
- ✅ Test : Rapport contient les compteurs corrects

**Mocks nécessaires** :
- `MonsterService`
- `SkillsService`
- `MonstersApiClient`
- `PlayerApiClient`
- `InvocationBufferRepository`

---

#### **1.1.2 MonsterService** (Priorité HAUTE)

**Fichier** : `MonsterServiceTest.java`

- ✅ Test : `createMonster()` sauvegarde et retourne l'ID
- ✅ Test : `getMonsterById()` retourne le bon monstre
- ✅ Test : `getMonsterById()` retourne null si inexistant
- ✅ Test : `getAllMonsters()` retourne tous les monstres
- ✅ Test : `getAllMonsterIdByRank()` filtre correctement par rank
- ✅ Test : `updateMonster()` met à jour les données
- ✅ Test : `deleteMonsterById()` supprime correctement
- ✅ Test : `getRandomMonsterByRank()` retourne un monstre du bon rank
- ✅ Test : `getRandomMonsterByRank()` lance exception si aucun monstre disponible
- ✅ Test : `hasAvailableData()` retourne true/false selon disponibilité

**Mock** : `MonsterRepository`

---

#### **1.1.3 SkillsService** (Priorité HAUTE)

**Fichier** : `SkillsServiceTest.java`

- ✅ Test : `createSkill()` vérifie que le monster existe
- ✅ Test : `createSkill()` lance IllegalArgumentException si monster inexistant
- ✅ Test : `getSkillById()` retourne le skill correct
- ✅ Test : `getSkillByMonsterId()` retourne tous les skills du monstre
- ✅ Test : `updateSkill()` met à jour correctement
- ✅ Test : `deleteSkillById()` supprime correctement
- ✅ Test : `deleteSkillByMonsterId()` supprime tous les skills du monstre
- ✅ Test : `getRandomSkillsForMonster()` retourne le nombre demandé de skills
- ✅ Test : `getRandomSkillsForMonster()` assigne les numéros 1, 2, 3 correctement
- ✅ Test : `hasAvailableData()` retourne true/false selon disponibilité

**Mocks** : `SkillsRepository`, `MonsterService`

---

#### **1.1.4 StatsService** (Priorité MOYENNE)

**Fichier** : `StatsServiceTest.java`

- ✅ Test : `verifyCorrectRanksDropRate()` retourne true si somme = 100%
- ✅ Test : `verifyCorrectRanksDropRate()` retourne false si somme ≠ 100%
- ✅ Test : `getTheoreticalLootRatesString()` format correct
- ✅ Test : `getRealLootRatesString()` calcule correctement les ratios
- ✅ Test : `getLootRatesString()` combine les deux outputs

**Mocks** : `MonsterService`, `SkillsService`

---

### 1.2 Clients API

#### **1.2.1 MonstersApiClient** (Priorité CRITIQUE)

**Fichier** : `MonstersApiClientTest.java`

- ✅ Test : `createMonster()` succès avec HTTP 201
- ✅ Test : `createMonster()` succès avec HTTP 200
- ✅ Test : `createMonster()` lance ExternalApiException sur HTTP 4xx
- ✅ Test : `createMonster()` lance ExternalApiException sur HTTP 5xx
- ✅ Test : `createMonster()` lance ExternalApiException si body null
- ✅ Test : `createMonster()` lance ExternalApiException sur RestClientException
- ✅ Test : `createMonster()` timeout géré correctement
- ✅ Test : `deleteMonster()` succès
- ✅ Test : `deleteMonster()` log warning mais ne lance pas exception sur échec

**Mocks** : `RestTemplate`, `ExternalApiProperties`

---

#### **1.2.2 PlayerApiClient** (Priorité CRITIQUE)

**Fichier** : `PlayerApiClientTest.java`

- ✅ Test : `addMonsterToPlayer()` succès avec HTTP 200
- ✅ Test : `addMonsterToPlayer()` vérifie le bearer token dans les headers
- ✅ Test : `addMonsterToPlayer()` lance ExternalApiException sur HTTP 4xx
- ✅ Test : `addMonsterToPlayer()` lance ExternalApiException sur HTTP 5xx
- ✅ Test : `addMonsterToPlayer()` gère RestClientException

**Mocks** : `RestTemplate`, `ExternalApiProperties`

---

#### **1.2.3 AuthApiClient** (Priorité MOYENNE)

**Fichier** : `AuthApiClientTest.java`

- ✅ Test : `authenticateService()` retourne token valide
- ✅ Test : `authenticateService()` gère erreur 401
- ✅ Test : `authenticateService()` gère erreur réseau

**Mocks** : `RestTemplate`, `ExternalApiProperties`

---

### 1.3 Mappers

#### **1.3.1 DtoMapperInvocation** (Priorité BASSE)

**Fichier** : `DtoMapperInvocationTest.java`

- ✅ Test : `toInvocationReplayResponse()` mapping complet
- ✅ Test : Tous les champs sont correctement mappés
- ✅ Test : Collections vides gérées

---

#### **1.3.2 DtoMapperMonster** (Priorité BASSE)

**Fichier** : `DtoMapperMonsterTest.java`

- ✅ Test : `toMonsterMongoDto()` depuis MonsterHttpDto
- ✅ Test : `toMonsterDto()` depuis MonsterMongoDto
- ✅ Test : Tous les champs mappés correctement
- ✅ Test : Enums convertis correctement

---

#### **1.3.3 DtoMapperSkills** (Priorité BASSE)

**Fichier** : `DtoMapperSkillsTest.java`

- ✅ Test : `toSkillsMongoDto()` depuis SkillsHttpDto
- ✅ Test : `toSkillsDto()` depuis SkillsMongoDto
- ✅ Test : Tous les champs mappés correctement

---

### 1.4 Utilitaires

#### **1.4.1 Random** (Priorité HAUTE)

**Fichier** : `RandomTest.java`

- ✅ Test : `random(min, max)` retourne valeur dans la plage
- ✅ Test : Distribution uniforme sur 10000 appels
- ✅ Test : `getRandomRankBasedOnAvailableData()` respecte les probabilités configurées
- ✅ Test : `getRandomRankBasedOnAvailableData()` skip les ranks sans data
- ✅ Test : `getRandomRankBasedOnAvailableData()` lance exception si aucun rank disponible

---

#### **1.4.2 AuthHandler** (Priorité MOYENNE)

**Fichier** : `AuthHandlerTest.java`

- ✅ Test : `getToken()` retourne token en cache si valide
- ✅ Test : `getToken()` rafraichit token si expiré
- ✅ Test : Token initialisé au premier appel

**Mocks** : `AuthApiClient`

---

### 1.5 Exception Handlers

#### **1.5.1 GlobalExceptionHandler** (Priorité HAUTE)

**Fichier** : `GlobalExceptionHandlerTest.java`

- ✅ Test : `handleExternalApiException()` retourne 502 avec message
- ✅ Test : `handleIllegalArgumentException()` retourne 400 avec message
- ✅ Test : `handleValidationExceptions()` retourne 400 avec détails
- ✅ Test : `handleGenericException()` retourne 500

---

## 🔗 PARTIE 2 : Tests d'Intégration

### 2.1 Tests Repository avec MongoDB

**Configuration de base** : `MongoTestConfiguration.java`

```java
@Testcontainers
@SpringBootTest
public abstract class MongoDbTestBase {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}
```

#### **2.1.1 MonsterRepository** (Priorité HAUTE)

**Fichier** : `MonsterRepositoryIntegrationTest.java`

- ✅ Test : CRUD complet sur MongoDB réel
- ✅ Test : `findAllMonsterIdByRank()` filtre correctement
- ✅ Test : Index sur rank fonctionne
- ✅ Test : Contraintes de validation MongoDB
- ✅ Test : Recherche par ID inexistant retourne null

---

#### **2.1.2 SkillsRepository** (Priorité HAUTE)

**Fichier** : `SkillsRepositoryIntegrationTest.java`

- ✅ Test : CRUD complet
- ✅ Test : `findByMonsterId()` retourne tous les skills
- ✅ Test : `deleteByMonsterId()` supprime tous les skills
- ✅ Test : Cascade delete non implémenté (test de non-suppression auto)

---

#### **2.1.3 InvocationBufferRepository** (Priorité HAUTE)

**Fichier** : `InvocationBufferRepositoryIntegrationTest.java`

- ✅ Test : Sauvegarde d'invocation en buffer
- ✅ Test : `findByStatus()` retourne les bons éléments
- ✅ Test : Mise à jour du status
- ✅ Test : Increment attemptCount
- ✅ Test : Timestamps createdAt et lastAttemptAt

---

### 2.2 Tests Controllers avec MockMvc

**Configuration** : Utiliser `@WebMvcTest` pour isoler les controllers

#### **2.2.1 InvocationController** (Priorité CRITIQUE)

**Fichier** : `InvocationControllerIntegrationTest.java`

- ✅ Test : `GET /api/invocation/invoque` retourne 200 avec GlobalMonsterDto
- ✅ Test : `POST /api/invocation/global-invoque/{playerId}` retourne 200
- ✅ Test : `POST /api/invocation/global-invoque/{playerId}` retourne 502 si API externe fail
- ✅ Test : `POST /api/invocation/recreate` retourne rapport
- ✅ Test : Validation des path variables

**Mocks** : `InvocationService`, `DtoMapperInvocation`

---

#### **2.2.2 MonsterController** (Priorité HAUTE)

**Fichier** : `MonsterControllerIntegrationTest.java`

- ✅ Test : `POST /api/invocation/monsters/create` retourne 201 avec ID
- ✅ Test : Validation du body (champs requis)
- ✅ Test : `GET /api/invocation/monsters/{id}` retourne 200
- ✅ Test : `GET /api/invocation/monsters/{id}` retourne 404 si inexistant
- ✅ Test : `GET /api/invocation/monsters/all` retourne liste
- ✅ Test : `PUT /api/invocation/monsters/{id}` met à jour
- ✅ Test : `DELETE /api/invocation/monsters/{id}` supprime

**Mocks** : `MonsterService`, `DtoMapperMonster`

---

#### **2.2.3 SkillsController** (Priorité HAUTE)

**Fichier** : `SkillsControllerIntegrationTest.java`

- ✅ Test : `POST /api/invocation/skills` retourne 201
- ✅ Test : Validation des contraintes (@Valid)
- ✅ Test : `GET /api/invocation/skills/{id}` retourne 200
- ✅ Test : `GET /api/invocation/skills/monster/{monsterId}` retourne liste
- ✅ Test : `PUT /api/invocation/skills/{id}` met à jour
- ✅ Test : `DELETE /api/invocation/skills/{id}` supprime

**Mocks** : `SkillsService`, `DtoMapperSkills`

---

#### **2.2.4 StatsController** (Priorité MOYENNE)

**Fichier** : `StatsControllerIntegrationTest.java`

- ✅ Test : `GET /api/invocation/stats/verify-ranks-drop-rate` retourne message
- ✅ Test : `GET /api/invocation/stats/get-loot-rate?type=all`
- ✅ Test : `GET /api/invocation/stats/get-loot-rate?type=theoretical`
- ✅ Test : `GET /api/invocation/stats/get-loot-rate?type=real`

**Mock** : `StatsService`

---

### 2.3 Tests Clients API avec WireMock

**Configuration** : `WireMockTestBase.java`

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class WireMockTestBase {
    
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("external.api.monsters.base-url", 
            () -> wireMock.baseUrl());
        registry.add("external.api.player.base-url", 
            () -> wireMock.baseUrl());
    }
}
```

#### **2.3.1 MonstersApiClient Integration** (Priorité CRITIQUE)

**Fichier** : `MonstersApiClientIntegrationTest.java`

- ✅ Test : Appel réel avec WireMock simulant l'API Monsters
- ✅ Test : Stub pour créer un monstre (201)
- ✅ Test : Stub pour erreur 500
- ✅ Test : Stub pour timeout
- ✅ Test : Vérification des headers envoyés

---

#### **2.3.2 PlayerApiClient Integration** (Priorité CRITIQUE)

**Fichier** : `PlayerApiClientIntegrationTest.java`

- ✅ Test : Appel réel avec WireMock
- ✅ Test : Vérification du Bearer token
- ✅ Test : Simulation erreur 404 (joueur inexistant)

---

### 2.4 Tests de Configuration

#### **2.4.1 AuthInterceptor** (Priorité MOYENNE)

**Fichier** : `AuthInterceptorIntegrationTest.java`

- ✅ Test : Token ajouté automatiquement aux requêtes sortantes
- ✅ Test : Token rafraîchi si expiré

---

#### **2.4.2 BearerTokenRestTemplateInterceptor** (Priorité BASSE)

**Fichier** : `BearerTokenRestTemplateInterceptorTest.java`

- ✅ Test : Bearer token ajouté au header Authorization

---

### 2.5 Tests de DatabaseSeeder

**Fichier** : `DatabaseSeederIntegrationTest.java`

- ✅ Test : Données initiales insérées au démarrage
- ✅ Test : Pas de doublon si relancé
- ✅ Test : Tous les ranks ont des monstres
- ✅ Test : Tous les monstres ont des skills

---

## 🌍 PARTIE 3 : Tests End-to-End (E2E)

**Configuration** : Application complète + MongoDB + WireMock pour APIs externes

### 3.1 Scénario E2E Complet

#### **3.1.1 Test E2E : Workflow d'Invocation Réussi**

**Fichier** : `InvocationE2ETest.java`

**Scénario** :
1. ✅ Client appelle `POST /api/invocation/global-invoque/{playerId}`
2. ✅ Service génère un monstre aléatoire
3. ✅ Appel à l'API Monsters (WireMock) pour créer le monstre → 201
4. ✅ Appel à l'API Player (WireMock) pour ajouter au joueur → 200
5. ✅ Buffer d'invocation marqué COMPLETED
6. ✅ Response 200 avec GlobalMonsterDto retournée

**Vérifications** :
- Buffer créé en BDD avec status COMPLETED
- WireMock a reçu 2 appels (monsters + player)
- Headers de requêtes corrects

---

#### **3.1.2 Test E2E : Échec API Monsters**

**Scénario** :
1. ✅ Client appelle `POST /api/invocation/global-invoque/{playerId}`
2. ✅ API Monsters retourne 500
3. ✅ Exception propagée
4. ✅ Buffer marqué FAILED_MONSTER_CREATION
5. ✅ Response 502

**Vérifications** :
- Aucun appel à l'API Player
- Pas de compensation (car monstre jamais créé)

---

#### **3.1.3 Test E2E : Échec API Player avec Compensation**

**Scénario** :
1. ✅ Client appelle `POST /api/invocation/global-invoque/{playerId}`
2. ✅ API Monsters créé monstre avec succès → monsterId = "abc123"
3. ✅ API Player retourne 404 (joueur inexistant)
4. ✅ **Compensation** : appel DELETE à l'API Monsters pour supprimer "abc123"
5. ✅ Buffer marqué FAILED_PLAYER_ADD
6. ✅ Response 502

**Vérifications** :
- WireMock a reçu 3 appels : POST monsters, POST player, DELETE monsters
- Buffer contient les détails de l'erreur

---

#### **3.1.4 Test E2E : Replay des Invocations Bufferisées**

**Scénario** :
1. ✅ Créer manuellement 3 buffers en BDD :
   - 1 PENDING
   - 1 FAILED_PLAYER_ADD (avec monsterId)
   - 1 COMPLETED
2. ✅ Client appelle `POST /api/invocation/recreate`
3. ✅ Service rejoue uniquement PENDING et FAILED_PLAYER_ADD
4. ✅ API Monsters/Player simulés avec succès

**Vérifications** :
- Rapport contient 2 succès, 0 échec
- COMPLETED ignoré
- Tous les buffers passent à COMPLETED

---

### 3.2 Tests de Performance et Charge (Optionnel)

**Fichier** : `LoadTest.java` (avec JMeter ou Gatling)

- ✅ Test : 100 invocations concurrentes
- ✅ Test : Temps de réponse < 500ms
- ✅ Test : Pas de race condition sur le random

---

## 📊 PARTIE 4 : Couverture et Qualité

### 4.1 Configuration JaCoCo

**Ajouter au pom.xml** :

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
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
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 4.2 Commandes Maven

```bash
# Lancer tous les tests
./mvnw test

# Lancer uniquement les tests unitaires
./mvnw test -Dtest=*Test

# Lancer uniquement les tests d'intégration
./mvnw test -Dtest=*IntegrationTest

# Lancer uniquement les tests E2E
./mvnw test -Dtest=*E2ETest

# Générer le rapport de couverture
./mvnw clean test jacoco:report

# Rapport disponible dans : target/site/jacoco/index.html
```

---

## 📝 PARTIE 5 : Bonnes Pratiques

### 5.1 Conventions de Nommage

- **Tests unitaires** : `ClasseTest.java`
- **Tests d'intégration** : `ClasseIntegrationTest.java`
- **Tests E2E** : `FeatureE2ETest.java`
- **Méthodes de test** : `should_ReturnExpectedResult_When_Condition()`

### 5.2 Structure AAA (Arrange-Act-Assert)

```java
@Test
void should_CreateMonster_When_ValidRequest() {
    // Arrange (Given)
    MonsterMongoDto monster = new MonsterMongoDto(...);
    when(repository.save(any())).thenReturn("id123");
    
    // Act (When)
    String result = service.createMonster(monster);
    
    // Assert (Then)
    assertThat(result).isEqualTo("id123");
    verify(repository, times(1)).save(monster);
}
```

### 5.3 Utilisation d'AssertJ

```java
// Plutôt que
assertEquals(expected, actual);

// Utiliser
assertThat(actual).isEqualTo(expected);

// Pour les collections
assertThat(list)
    .hasSize(3)
    .containsExactly(item1, item2, item3);

// Pour les exceptions
assertThatThrownBy(() -> service.method())
    .isInstanceOf(ExternalApiException.class)
    .hasMessageContaining("expected error");
```

### 5.4 Tests Paramétriques

```java
@ParameterizedTest
@EnumSource(Rank.class)
void should_HandleAllRanks(Rank rank) {
    // Test pour chaque rank
}

@ParameterizedTest
@CsvSource({
    "COMMON, 0.5",
    "RARE, 0.3",
    "EPIC, 0.15",
    "LEGENDARY, 0.05"
})
void should_RespectDropRates(Rank rank, double expectedRate) {
    // Test
}
```

### 5.5 Fixtures et Builders

**Créer des builders de test** :

```java
public class MonsterTestBuilder {
    public static MonsterMongoDto aDefaultMonster() {
        return new MonsterMongoDto(
            "test-id",
            Elementary.FEU,
            100L, 50L, 30L, 40L,
            Rank.COMMON
        );
    }
    
    public static MonsterMongoDto aLegendaryMonster() {
        return new MonsterMongoDto(
            "legendary-id",
            Elementary.TERRE,
            500L, 200L, 150L, 180L,
            Rank.LEGENDARY
        );
    }
}
```

---

## 🚀 PARTIE 6 : Plan d'Implémentation

### Phase 1 : Tests Unitaires Critiques (Semaine 1)
1. InvocationService
2. MonstersApiClient
3. PlayerApiClient
4. Random utility
5. GlobalExceptionHandler

### Phase 2 : Tests Unitaires Complets (Semaine 2)
1. MonsterService
2. SkillsService
3. StatsService
4. Tous les mappers
5. AuthHandler

### Phase 3 : Tests d'Intégration (Semaine 3)
1. Setup Testcontainers + MongoDB
2. Tests Repository
3. Tests Controllers avec MockMvc
4. Setup WireMock
5. Tests Clients API

### Phase 4 : Tests E2E (Semaine 4)
1. Configuration environnement E2E complet
2. Scénarios de succès
3. Scénarios d'échec avec compensation
4. Tests de replay
5. Optimisation et refactoring

### Phase 5 : Qualité et Documentation (Semaine 5)
1. Atteindre objectifs de couverture
2. Review et amélioration
3. Documentation des tests
4. CI/CD pipeline

PS: seules les Phases 1 et 2 ont été implémentés pour l'instant
---

## 📍 Checklist Finale

- [ ] Tous les services ont >90% de couverture
- [ ] Tous les controllers ont >85% de couverture
- [ ] Pattern Saga testé avec compensation
- [ ] Tous les cas d'erreur couverts
- [ ] Tests d'intégration avec MongoDB fonctionnels
- [ ] WireMock simule correctement les APIs externes
- [ ] Tests E2E passent avec succès
- [ ] Rapport JaCoCo généré
- [ ] CI/CD pipeline intègre les tests
- [ ] Documentation à jour

---

## 🔗 Ressources

- [Spring Boot Testing](https://spring.io/guides/gs/testing-web)
- [Testcontainers](https://testcontainers.com/)
- [WireMock](https://wiremock.org/)
- [AssertJ](https://assertj.github.io/doc/)
- [JaCoCo](https://www.jacoco.org/jacoco/)
