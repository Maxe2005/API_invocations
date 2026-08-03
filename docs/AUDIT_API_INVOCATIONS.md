# Audit complet - API Invocations

Date : 2026-08-03
Remplace l'audit du 2026-02-05, qui portait encore sur l'ancienne implémentation MongoDB (`MonsterMongoDto`, `SkillsMongoDto`) — le service tourne aujourd'hui sur **PostgreSQL + JPA** (Flyway, `ddl-auto: validate`). Les constats de l'ancien audit encore valides ont été repris et vérifiés contre le code actuel ; ceux devenus obsolètes (endpoint `global-invoque` en GET, incohérence de port 8085, `MonsterMongoDto`...) ont été retirés.

## Portée

Code source (`src/main/java/com/imt/api_invocations`), tests (`src/test`), configuration (`application.yml`, `application-local.yml`, `checkstyle.xml`, `pom.xml`), migrations Flyway (`src/main/resources/db/migration`), et documentation (`docs/`, `README.md`, `TODO.md`).

---

## Points positifs

- **Architecture en couches cohérente**, répétée à l'identique sur les 4 domaines (monster/skill/invocation/stats) : controller → service → repository-wrapper → JPA, avec mappers dédiés. Navigable par analogie d'un domaine à l'autre.
- **Pattern Saga avec compensation** bien isolé dans [`InvocationService`](../src/main/java/com/imt/api_invocations/service/InvocationService.java) : buffer d'audit persistant (`invocation_buffer`, colonnes JSONB), transitions d'état explicites (`PENDING` → `MONSTER_CREATED` → `COMPLETED`/`FAILED`), rejeu possible (`replayBufferedInvocations`).
- **Schéma piloté par Flyway** (`ddl-auto: validate`) — pas de dérive Hibernate auto-générée, changements de schéma explicites et versionnés dans [`V1__init.sql`](../src/main/resources/db/migration/V1__init.sql).
- **Configuration centralisée** via `@ConfigurationProperties` (`ExternalApiProperties`, `NumericConstraintsConfig`, `AppProperties`) plutôt que des `@Value` éparpillés.
- **`@IntRange`** : annotation de validation maison qui va chercher ses bornes dans la config centralisée plutôt que de les dupliquer dans chaque DTO.
- **Swagger/OpenAPI soigné** : descriptions, exemples, codes de réponse par endpoint.
- **Outillage qualité présent** : Spotless (Google Java Format) + Checkstyle + script interactif `format-lint.sh` — l'intention est là même si l'exécution ne suit pas partout (voir plus bas).
- **Base de tests unitaires correcte sur le cœur métier** : `InvocationService`, `MonsterService`, `SkillsService`, `StatsService`, les clients, `Random`, les mappers de contrôleur.

---

## Points faibles

### Critiques (sécurité / risque fort)

| # | Constat | Fichier(s) |
|---|---|---|
| C1 | `app.auth.enabled: false` par défaut → tous les endpoints sont ouverts sans authentification dans la config livrée. | [application.yml](../src/main/resources/application.yml#L25), [WebConfig](../src/main/java/com/imt/api_invocations/config/WebConfig.java#L17) |
| C2 | Zéro `@NotNull`/`@NotBlank`/`@NotEmpty` dans `dto/` et `controller/dto/`, malgré des champs marqués `REQUIRED` dans Swagger. Validation reportée à la main et incomplète dans le mapper. | [DtoMapperMonster.toMonsterEntity](../src/main/java/com/imt/api_invocations/controller/mapper/DtoMapperMonster.java#L26) |
| C3 | IDOR sur `DELETE /api/monsters/delete/{id}` — pas de contrôle de propriétaire (déjà tracé dans [TODO.md](../TODO.md) point 2). | `MonstersApiClient` côté `API_monstres` |
| C4 | Zip bomb / DoS : dézippage entier en mémoire sans limite de taille/entrées/ratio sur l'import de monstres. | [MonsterImportExportService.importMonstersFromStream](../src/main/java/com/imt/api_invocations/service/MonsterImportExportService.java#L193) |
| C5 | URL MinIO hardcodée (`http://localhost:9000/raw-assets/monsters/...`) pour l'upload d'images importées, alors que la lecture utilise `assets.host` configurable ; échec silencieux en Docker (log `info`). | [MonsterImportExportService](../src/main/java/com/imt/api_invocations/service/MonsterImportExportService.java#L239) |

### Importantes (fiabilité)

| # | Constat | Fichier(s) |
|---|---|---|
| I1 | Timeouts déclarés (`connectionTimeout`/`readTimeout`) mais jamais appliqués au `RestTemplate`. | [RestClientConfig](../src/main/java/com/imt/api_invocations/config/RestClientConfig.java), [ExternalApiProperties](../src/main/java/com/imt/api_invocations/config/ExternalApiProperties.java) |
| I2 | État mutable partagé non thread-safe (`possibleSkills` en champ d'instance sur un bean singleton). | [SkillsService](../src/main/java/com/imt/api_invocations/service/SkillsService.java#L21) |
| I3 | `getRandomRankBasedOnAvailableData` retourne `null` si catalogue vide → `IllegalArgumentException` cryptique en aval au lieu d'une erreur métier claire. | [Random](../src/main/java/com/imt/api_invocations/utils/Random.java#L26) |
| I4 | Pas de handler générique `Exception.class` — les exceptions non listées (ex. `IllegalStateException`) échappent au format `Errors` maison. | [GlobalExceptionHandler](../src/main/java/com/imt/api_invocations/exception/GlobalExceptionHandler.java) |
| I5 | `attemptCount` incrémenté mais jamais comparé à un plafond — `/api/invocation/recreate` peut rejouer indéfiniment sans backoff. | [InvocationService](../src/main/java/com/imt/api_invocations/service/InvocationService.java) |
| I6 | Échec de compensation (`deleteMonster` pendant le rollback) seulement loggé, jamais retenté ni requeue — incohérence permanente possible (monstre orphelin). | [InvocationService.executeInvocation](../src/main/java/com/imt/api_invocations/service/InvocationService.java#L127) |

### Moyennes (qualité / cohérence)

| # | Constat | Fichier(s) |
|---|---|---|
| M1 | `AvoidStarImport` activé dans Checkstyle mais 8 fichiers utilisent des imports `*`. | `InvocationController`, `MonsterController`, `SkillsController`, `StatsController`, `InvocationService`, `SkillsService`, `StatsService`, `IntRange` |
| M2 | Aucune CI (`.github/workflows` absent) — `spotless:check`/`checkstyle:check`/`mvn test` sont manuels uniquement. | — |
| M3 | Formatage incohérent (fichiers jamais passés dans `spotless:apply` : indentation différente, noms pleinement qualifiés inline). | `MonsterController`, `SkillsController`, `PlayerApiClient`, `MonsterImportExportService` |
| M4 | Confusion `username`/`playerId` : le paramètre reçu est en réalité un `playerId`. | [PlayerApiClient.addMonsterToPlayer](../src/main/java/com/imt/api_invocations/client/PlayerApiClient.java#L41) |
| M5 | Doc/code désynchronisés : plusieurs docs décrivent encore MongoDB/Testcontainers/WireMock jamais implémentés. | `docs/ARCHITECTURE_INTER_API.md`, `docs/SAGA_PATTERN.md`, `docs/SYSTEME_INVOCATION.md`, `docs/MODULAR_DATABASE_SEEDING.md`, `docs/STRATEGIE_TESTS.md` |
| M6 | Collision de port en dev local : `server.port: 8081` entre en conflit avec le port hôte de `api-authentification` dans la stack Docker. | [application-local.yml](../src/main/resources/application-local.yml#L13) |
| M7 | Nom de champ blagueur exposé dans le schéma d'erreur public. | [Errors.theErrorsYOUMade](../src/main/java/com/imt/api_invocations/exception/Errors.java#L11) |

---

## Manques

- **G1** — Aucun test sur les controllers (`Monster`, `Skills`, `Stats`, `Invocation`, `ImportExport` — 0 `@WebMvcTest`).
- **G2** — Aucun test sur `AuthInterceptor`.
- **G3** — Aucun test sur `DatabaseSeeder`/`MonsterSeedingService`.
- **G4** — Aucun test sur `MonsterImportExportService` (le fichier le plus complexe et le moins propre du repo).
- **G5** — Pas de retry/circuit breaker (Resilience4j évoqué comme "amélioration future" dans la doc, jamais implémenté).
- **G6** — Pas de pagination sur `GET /monsters/all`.
- **G7** — Pas de limite de taille explicite sur l'upload `/monsters/import`.
- **G8** — Pas de clé d'idempotence sur `POST /global-invoque/{playerId}`.

## Excès / dette

- **E1** — Double mécanisme de merge partiel pour les stats (`mergeStats` sentinelle-`0`, mort, vs `mergeStatsWithUpdate` nullable, la bonne approche) ; overload `updateMonsterEntity(MonsterEntity, MonsterHttpDto)` jamais appelé.
- **E2** — `AuthHandler` (`utils/`) n'est référencé nulle part ailleurs que dans son propre fichier et son test — duplique `AuthApiClient.verifyToken` sans être utilisé.
- **E3** — `ExternalApiClient` (interface ISP) implémentée seulement par `AuthApiClient`/`ApiGenerateGatchaClient`, pas par `MonstersApiClient`/`PlayerApiClient` — abstraction appliquée à moitié.

---

## Roadmap de correction

Organisée en phases exécutables dans l'ordre. Chaque item référence l'identifiant du constat ci-dessus. Les phases 0-1 sont sécurité/fiabilité et devraient être traitées avant tout ajout de fonctionnalité ; les phases 2-4 peuvent se paralléliser.

### Phase 0 — Sécurité immédiate (avant toute mise en prod réelle)

1. **[C1]** Décider explicitement de la politique d'auth par environnement : au minimum un profil `prod`/`docker` avec `app.auth.enabled: true` par défaut, `local`/`dev` pouvant rester désactivé. Documenter le choix dans `application.yml` (commentaire) et le root `README`.
2. **[C4]** Borner l'import ZIP avant dézippage : taille max du fichier uploadé (`spring.servlet.multipart.max-file-size`/`max-request-size` dans `application.yml`), nombre max d'entrées, et taille max décompressée cumulée dans `MonsterImportExportService.importMonstersFromStream` (abandon avec erreur explicite si dépassement).
3. **[C5]** Remplacer l'URL hardcodée `http://localhost:9000/...` par la propriété `assets.host` déjà utilisée côté export, dans `MonsterImportExportService` ; relever le niveau de log de l'échec d'upload à `warn` a minima.
4. **[C3]** Trancher l'item 2 de `TODO.md` (IDOR delete monstre) : soit ajouter une résolution `username` → `playerId` + contrôle de propriété avec exception explicite pour le flux de compensation de saga (token du joueur d'origine), soit documenter formellement pourquoi ce n'est pas fait maintenant. Ne pas laisser en l'état sans décision explicite.

### Phase 1 — Fiabilité (saga, résilience)

5. **[I1]** Câbler `connectionTimeout`/`readTimeout` sur le `RestTemplate` dans `RestClientConfig` via `RestTemplateBuilder.setConnectTimeout(...)`/`.setReadTimeout(...)` en lisant `ExternalApiProperties`.
6. **[I2]** Rendre `SkillsService.possibleSkills` local à `getRandomSkillsForMonster` (variable locale ou petit objet de contexte passé en paramètre à `hasAvailableData`) au lieu d'un champ d'instance.
7. **[I3]** Faire lever une exception métier explicite (ex. `IllegalStateException("Aucun monstre disponible pour l'invocation")`) dans `invoke()`/`Random` quand `getRandomRankBasedOnAvailableData` ne trouve aucun rang disponible, plutôt que de laisser `null` se propager.
8. **[I4]** Ajouter un `@ExceptionHandler(Exception.class)` de repli dans `GlobalExceptionHandler`, retournant un `Errors` avec code 500 générique et logguant la stack trace complète.
9. **[I5]** Introduire un plafond configurable (`app.invocation.max-attempts`) vérifié dans `replayBufferedInvocations`/`executeInvocation` ; marquer `FAILED` de façon terminale (ou un nouveau statut `ABANDONED`) au-delà, avec logging clair.
10. **[I6]** Ajouter une deuxième tentative (ou une file de compensation différée) quand `deleteMonster` échoue pendant le rollback, pour ne pas laisser d'incohérence permanente silencieuse ; à défaut, exposer ces échecs de compensation dans un endpoint/alerting dédié.

### Phase 2 — Outillage et hygiène de code

11. **[M2]** Ajouter une CI minimale (GitHub Actions) exécutant `./mvnw clean verify spotless:check checkstyle:check` sur chaque PR vers `development`/`master`.
12. **[M1, M3]** Lancer `mvn spotless:apply` sur l'ensemble du module pour uniformiser le formatage et faire disparaître les imports `*` (`AvoidStarImport` redeviendra effectif une fois la CI en place).
13. **[M4]** Renommer le paramètre `username` en `playerId` dans `PlayerApiClient.addMonsterToPlayer` pour refléter la réalité de l'espace d'identité utilisé.
14. **[M6]** Choisir un port de dev local qui n'entre pas en collision avec les ports hôte de la stack Docker (ex. `8090`) dans `application-local.yml`.
15. **[M7]** Renommer `Errors.theErrorsYOUMade` en `errors` (impact : mettre à jour le contrat OpenAPI/Swagger et prévenir les consommateurs de l'API, notamment `Gatcha_Front`).
16. **[M5]** Mettre à jour `ARCHITECTURE_INTER_API.md`, `SAGA_PATTERN.md`, `SYSTEME_INVOCATION.md`, `MODULAR_DATABASE_SEEDING.md` pour refléter Postgres/JPA ; réduire `STRATEGIE_TESTS.md` à ce qui est réellement outillé (Mockito/AssertJ) ou explicitement marquer le reste comme roadmap non engagée.

### Phase 3 — Validation et suppression de la dette

17. **[C2]** Ajouter les annotations Bean Validation (`@NotNull`, `@NotBlank`, `@Valid` en cascade) sur `MonsterBaseDto`, `SkillBaseDto`, `RatioDto`, `StatsDto` et leurs DTOs HTTP ; supprimer la validation manuelle redondante dans `DtoMapperMonster.toMonsterEntity` une fois couverte déclarativement.
18. **[E1]** Supprimer `mergeStats`/`updateMonsterEntity(MonsterEntity, MonsterHttpDto)` (code mort) dans `DtoMapperMonster`, ne garder que le chemin `MonsterHttpUpdateDto`/`mergeStatsWithUpdate`.
19. **[E2]** Supprimer `AuthHandler` et `AuthHandlerTest` (code et test morts), ou le brancher réellement si une utilité future est identifiée.
20. **[E3]** Soit faire implémenter `ExternalApiClient` par `MonstersApiClient`/`PlayerApiClient` pour cohérence, soit supprimer l'interface si elle n'apporte pas de valeur réelle (pas de polymorphisme exploité ailleurs qu'en logging).

### Phase 4 — Tests

21. **[G1]** Ajouter des `@WebMvcTest` pour `MonsterController`, `SkillsController`, `StatsController`, `InvocationController`, `ImportExportController` (cas nominal + validation + 404).
22. **[G2]** Ajouter un test d'intégration pour `AuthInterceptor` (token valide/invalide/absent, cookie vs header, OPTIONS laissé passer).
23. **[G3]** Ajouter des tests pour `DatabaseSeeder`/`MonsterSeedingService` (seed initial, seed skills-only si monstres déjà présents, gestion des violations de contrainte).
24. **[G4]** Ajouter des tests pour `MonsterImportExportService` (export avec/sans images, import valide, import malformé, limites de taille une fois la phase 0 appliquée).

### Phase 5 — Fonctionnalités manquantes (à planifier selon besoin produit)

25. **[G5]** Introduire Resilience4j (retry + circuit breaker) sur `MonstersApiClient`/`PlayerApiClient`/`AuthApiClient`/`ApiGenerateGatchaClient`.
26. **[G6]** Paginer `GET /monsters/all` (`page`/`size`) une fois le catalogue significatif.
27. **[G7]** Fixer une limite explicite (`spring.servlet.multipart.max-file-size`) plutôt que de dépendre des défauts Spring — couplé à l'item 2 de la phase 0.
28. **[G8]** Ajouter une clé d'idempotence (header `Idempotency-Key` ou dérivée d'un ID côté client) sur `POST /global-invoque/{playerId}` pour dédupliquer les retries client.

---

## Conclusion

L'architecture reste solide : la séparation des responsabilités et le pattern Saga avec buffer d'audit sont bien pensés et globalement bien exécutés. Les risques principaux se concentrent sur trois axes indépendants du design global : **la posture de sécurité par défaut** (auth désactivée, validation d'entrée absente, import non borné), **la résilience réseau** (timeouts non câblés, pas de retry, compensation best-effort) et **l'hygiène d'outillage** (pas de CI, linter non respecté, dette de code mort). Aucun de ces points ne remet en cause l'architecture ; ce sont des corrections ciblées, listées phase par phase ci-dessus, qui apporteront un gain rapide en fiabilité et en maintenabilité sans réécriture.
