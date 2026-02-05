# Audit complet - API Invocations

Date: 2026-02-05

## Portee
Audit base sur le code source, la configuration et la documentation dans:
- [pom.xml](../pom.xml)
- [README.md](../README.md)
- [docs/ARCHITECTURE_INTER_API.md](ARCHITECTURE_INTER_API.md)
- [docs/README_INTER_API.md](README_INTER_API.md)
- [src/main/java/com/imt/api_invocations](../src/main/java/com/imt/api_invocations)
- [src/main/resources](../src/main/resources)
- [Dockerfile](../Dockerfile)
- [docker-compose.yml](../docker-compose.yml)
- [src/test/java/com/imt/api_invocations](../src/test/java/com/imt/api_invocations)

## Points positifs (architecture, dependances, coherence)
- Separation claire des couches (controller/service/persistence/client/mappers/config) et respect global des responsabilites, par ex. [src/main/java/com/imt/api_invocations/service/InvocationService.java](../src/main/java/com/imt/api_invocations/service/InvocationService.java) et [src/main/java/com/imt/api_invocations/client/MonstersApiClient.java](../src/main/java/com/imt/api_invocations/client/MonstersApiClient.java).
- Pattern Saga avec compensation bien isole dans `InvocationService`, avec buffer d'invocation persistant pour rejouer, cf. [src/main/java/com/imt/api_invocations/persistence/dto/InvocationBufferDto.java](../src/main/java/com/imt/api_invocations/persistence/dto/InvocationBufferDto.java).
- Utilisation de `@ConfigurationProperties` pour centraliser les URLs et timeouts externes, cf. [src/main/java/com/imt/api_invocations/config/ExternalApiProperties.java](../src/main/java/com/imt/api_invocations/config/ExternalApiProperties.java).
- Documentation d'architecture solide et coherente avec les grands principes (SOLID/DRY) dans [docs/ARCHITECTURE_INTER_API.md](ARCHITECTURE_INTER_API.md).
- Usage de Spring Validation et DTOs de mise a jour partielle (update) pour les objets imbriques, cf. [src/main/java/com/imt/api_invocations/controller/dto/input/MonsterHttpUpdateDto.java](../src/main/java/com/imt/api_invocations/controller/dto/input/MonsterHttpUpdateDto.java).
- OpenAPI configure pour la securite bearer JWT, cf. [src/main/java/com/imt/api_invocations/config/OpenApiConfig.java](../src/main/java/com/imt/api_invocations/config/OpenApiConfig.java).
- Pipeline Docker multi-stage pour produire un jar leger, cf. [Dockerfile](../Dockerfile).

## Points a ameliorer (priorises)
### Critiques / Risques forts
- **Compilation et coherence Lombok**: le code instancie `MonsterMongoDto` et `SkillsMongoDto` via des constructeurs explicites qui n'existent pas avec `@SuperBuilder` seul. Exemple dans [src/main/java/com/imt/api_invocations/config/DatabaseSeeder.java](../src/main/java/com/imt/api_invocations/config/DatabaseSeeder.java) alors que les classes ciblees n'exposent pas de constructeur public dans [src/main/java/com/imt/api_invocations/persistence/dto/MonsterMongoDto.java](../src/main/java/com/imt/api_invocations/persistence/dto/MonsterMongoDto.java) et [src/main/java/com/imt/api_invocations/persistence/dto/SkillsMongoDto.java](../src/main/java/com/imt/api_invocations/persistence/dto/SkillsMongoDto.java). A corriger en utilisant les builders, ou en ajoutant des constructeurs Lombok explicites.
- **Endpoint non idempotent expose en GET**: `globalInvoke` effectue des creations et appels externes, mais est declare en `GET` dans [src/main/java/com/imt/api_invocations/controller/InvocationController.java](../src/main/java/com/imt/api_invocations/controller/InvocationController.java). Risque de re-execution (cache, prefetch, crawlers). Passer a `POST` et aligner la doc.
- **Choix aleatoire sans garde sur collection vide**: `MonsterService.getRandomMonsterByRank` peut lever une exception si aucun monstre pour un rang (index -1), cf. [src/main/java/com/imt/api_invocations/service/MonsterService.java](../src/main/java/com/imt/api_invocations/service/MonsterService.java). Le chemin amont `getRandomRankBasedOnAvailableData` peut retourner `null` (et donc provoquer un NPE) si aucun rang disponible, cf. [src/main/java/com/imt/api_invocations/utils/Random.java](../src/main/java/com/imt/api_invocations/utils/Random.java). A securiser avec erreurs metier explicites.
- **Etat partage non thread-safe dans SkillsService**: le champ `possibleSkills` est mutable et stocke au niveau de la classe, ce qui est unsafe en environnement multi-thread et fausse `hasAvailableData`, cf. [src/main/java/com/imt/api_invocations/service/SkillsService.java](../src/main/java/com/imt/api_invocations/service/SkillsService.java). A rendre local a la methode ou a passer via un objet contexte.

### Importantes / Qualite et robustesse
- **Timeouts declares mais non appliques**: les proprietes `connectionTimeout` et `readTimeout` ne sont pas utilisees dans [src/main/java/com/imt/api_invocations/config/RestClientConfig.java](../src/main/java/com/imt/api_invocations/config/RestClientConfig.java). Il faut configurer `RestTemplateBuilder` avec ces valeurs ou migrer vers `WebClient`.
- **Gestion d'erreurs de validation incomplete**: seul `HandlerMethodValidationException` est intercepte, mais les erreurs de validation `@Valid` sur body peuvent lever `MethodArgumentNotValidException`. Completer [src/main/java/com/imt/api_invocations/exception/GlobalExceptionHandler.java](../src/main/java/com/imt/api_invocations/exception/GlobalExceptionHandler.java).
- **Validations non uniformes**: la validation est partiellement codee dans les mappers (ex. `DtoMapperMonster`, `DtoMapperSkills`) alors que les DTOs n'ont pas de `@NotNull`/`@NotBlank` sur les champs requis, cf. [src/main/java/com/imt/api_invocations/controller/mapper/DtoMapperMonster.java](../src/main/java/com/imt/api_invocations/controller/mapper/DtoMapperMonster.java) et [src/main/java/com/imt/api_invocations/controller/dto/input/MonsterHttpDto.java](../src/main/java/com/imt/api_invocations/controller/dto/input/MonsterHttpDto.java). Uniformiser via bean validation.
- **Incoherences doc/code**:
  - L'endpoint de `global-invoque` est indique en `POST` dans [docs/ARCHITECTURE_INTER_API.md](ARCHITECTURE_INTER_API.md) mais code en `GET` dans [src/main/java/com/imt/api_invocations/controller/InvocationController.java](../src/main/java/com/imt/api_invocations/controller/InvocationController.java).
  - L'API Player mentionnee en doc (`/api/joueur/add_monster`) ne correspond pas a l'implementation (`/api/players/{username}/add_monster`), cf. [src/main/java/com/imt/api_invocations/client/PlayerApiClient.java](../src/main/java/com/imt/api_invocations/client/PlayerApiClient.java).
  - Le port local (8081) dans [src/main/resources/application-local.yml](../src/main/resources/application-local.yml) ne correspond pas a la doc (8085) dans [README.md](../README.md).
- **Rejouer les invocations sans limite**: `attemptCount` est incremente mais jamais utilise pour stopper ou backoff; un endpoint `recreate` peut reessayer indéfiniment, cf. [src/main/java/com/imt/api_invocations/service/InvocationService.java](../src/main/java/com/imt/api_invocations/service/InvocationService.java).

### Moyennes / Architecture et lisibilite
- **Repository custom peu optimal**: `MonsterRepository.findByElement` scanne toute la collection avec `findAll`, cf. [src/main/java/com/imt/api_invocations/persistence/MonsterRepository.java](../src/main/java/com/imt/api_invocations/persistence/MonsterRepository.java). Ajouter une requete Mongo.
- **Etat de tests**: la plupart des tests sont commentes et obsoletes, cf. [src/test/java/com/imt/api_invocations/service/InvocationServiceTest.java](../src/test/java/com/imt/api_invocations/service/InvocationServiceTest.java). Les tests restants sont minimalistes et ne couvrent pas l'integration Mongo ni les controllers.
- **Couverture d'erreurs de l'auth**: `AuthApiClient.verifyToken` renvoie `false` sur toute erreur reseau, ce qui peut couper l'app en cas d'indisponibilite, cf. [src/main/java/com/imt/api_invocations/client/AuthApiClient.java](../src/main/java/com/imt/api_invocations/client/AuthApiClient.java). Ajouter une strategie de fallback/timeout control.
- **Intercepteur auth global**: l'auth est desactivee par defaut dans [src/main/resources/application.yml](../src/main/resources/application.yml). Prevoir un profil prod qui active l'auth.

### Faibles / Hygiene et UX
- **Nommage et schema erreurs**: `Errors.theErrorsYOUMade` est peu neutre, cf. [src/main/java/com/imt/api_invocations/exception/Errors.java](../src/main/java/com/imt/api_invocations/exception/Errors.java). Renommer pour exposer un schema propre (`errors`).
- **Retours textuels non structures**: les endpoints stats renvoient des strings multi-lignes, cf. [src/main/java/com/imt/api_invocations/controller/StatsController.java](../src/main/java/com/imt/api_invocations/controller/StatsController.java). Proposer un format JSON pour consommation programmatique.

## Recommandations detaillees
### Court terme (1-3 jours)
- Passer `global-invoque` en `POST` et aligner la doc.
- Corriger les instanciations `new MonsterMongoDto(...)` et `new SkillsMongoDto(...)` en builders ou ajouter des constructeurs Lombok.
- Rendre `possibleSkills` local et rendre `hasAvailableData` deterministe.
- Ajouter la gestion de `MethodArgumentNotValidException` et uniformiser la validation (annotations `@NotNull`, `@NotBlank`).

### Moyen terme (1-2 semaines)
- Injecter les timeouts dans `RestTemplateBuilder` ou migrer vers `WebClient`.
- Introduire une limite et une strategie de retry (ex. `attemptCount` + backoff) pour `recreate`.
- Rendre les responses d'erreur et stats JSON (schemas stables).
- Remettre a jour et reactiver la suite de tests (unitaires + integration Mongo). 

### Long terme (roadmap)
- Resilience: circuit breaker, retries, timeouts uniformes (Resilience4j), tracing distribue.
- Observabilite: logs structurels et correlation IDs, metrics personnalisées pour les invocations.
- Consistance: definir des contrats OpenAPI partages avec les autres APIs (monsters, player, auth).

## Points de coherence globale
- L'architecture est globalement coherente, mais quelques divergences doc/code (endpoints, ports) doivent etre corrigees pour eviter des integrerations cassees.
- Les DTOs d'update partiel sont bien presentes, mais la validation reste trop dependante des mappers (risque de duplication et d'erreurs).

## Gaps de tests
- Aucun test actif ne couvre les controllers, l'auth, ou la persistance Mongo.
- Les tests commentes ne reflectent plus les signatures actuelles et doivent etre remis a jour.

## Conclusion
L'API montre une architecture solide et une segregation des responsabilites claire, avec un pattern Saga bien pense et une bonne structure de code. Les principaux risques se situent dans la robustesse (timeouts non appliques, etats partages non thread-safe), la coherence doc/code, et la couverture de tests. Une correction ciblee de ces points apportera un gain rapide en fiabilite et maintenabilite.
