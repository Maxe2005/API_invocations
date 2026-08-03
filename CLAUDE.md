# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This is `API_invocations`, the Spring Boot **orchestration hub** of the Gatcha game, checked out as a git submodule of the `GatchaApi` root repo — see the root repo's `CLAUDE.md` for the cross-service architecture and how the whole stack is run (root `docker-compose.yaml` is the only way to run it; this submodule has no `docker-compose.yml`/`.env` of its own — see below).

## Common commands

```bash
./mvnw clean package                          # build
./mvnw test                                   # full test suite
./mvnw test -Dtest=ClassName                  # single test class
./mvnw test -Dtest=ClassName#methodName       # single test method

mvn spotless:apply                            # format (Google Java Format)
mvn spotless:check                            # check formatting without applying
mvn checkstyle:check                          # lint (checkstyle.xml; warnings only — failsOnError=false)
./format-lint.sh                              # interactive menu wrapping the two above
```

Run locally against the dockerized dependencies (Postgres + the other APIs) with the `local` profile, which points at the ports the root compose stack exposes on `localhost` (Postgres `5433`, auth `8081`, joueur `8082`, monstres `8083`):
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```
From the root repo: `make restart-api-invocations` rebuilds+restarts just this service in the dockerized stack; Swagger UI is at `:8080/swagger-ui/index.html`.

This repo has no `.env`/`.env.example` of its own. Any new environment variable this service needs must be added to the root repo's `.env`/`.env.exemple` and wired into this service's `environment:` block in the root `docker-compose.yaml` — there is nowhere else the running stack would pick it up from.

## Git workflow (required)

For any piece of work beyond a trivial one-line fix: create a dedicated branch (`feat/...`, `fix/...`, `perf/...`) from the integration branch (`master` here, unless a `dev`/`development` branch exists), commit in atomic steps with conventional-commit messages in French (`feat:`/`fix:`/`perf:`/`docs:` plus a body explaining the why), then merge back with `--no-ff`. Never commit sizeable work directly on the integration branch.

## Architecture

`API_invocations` owns a **PostgreSQL** database (Flyway-migrated, `ddl-auto: validate` — schema changes go through `src/main/resources/db/migration/`, never through Hibernate auto-DDL) storing monsters, skills, and an invocation audit buffer, and it is the one service that talks to all the others: `API_authentification` (token verification), `API_monstres` (create/delete monster), `API_joueur` (add monster to inventory), and `API_generate_gatcha`.

### Request flow: controller → service → repository wrapper → Spring Data JPA
Each domain has a thin `*Repository` component (`persistence/MonsterRepository.java`, `persistence/InvocationBufferRepository.java`, `persistence/SkillsRepository.java`) that wraps the actual Spring Data interface (`persistence/repository/*JpaRepository.java`) and translates `Optional`s to nulls / exposes domain-shaped query methods (e.g. `findByID(id, includeSkills)`). Services depend on the wrapper, not the JPA interface directly — when adding a query, add it to the JPA interface and expose it through the wrapper rather than injecting the JPA repository elsewhere.

### The invocation saga (`InvocationService`)
`globalInvoke(playerId)` is the core orchestration flow, implementing a **saga with compensation** (see `docs/SAGA_PATTERN.md`, `docs/ARCHITECTURE_INTER_API.md`, `docs/SYSTEME_INVOCATION.md` for the original design write-ups — treat their MongoDB/Testcontainers/WireMock mentions as historical/aspirational, since the service actually runs on **PostgreSQL + JPA** and none of those test dependencies are in `pom.xml`; `docs/STRATEGIE_TESTS.md` similarly describes a fuller test plan than what's implemented — only its Phases 1–2 (unit tests) exist today):

1. `invoke()` picks a random `Rank` for the monster (`Random.getRandomRankBasedOnAvailableData`, weighted COMMON 50% / RARE 30% / EPIC 15% / LEGENDARY 5%, **dynamically renormalized** when a rank has no data left — see `docs/SYSTEME_INVOCATION.md` for the math), pulls a random monster of that rank, and independently rolls 3 skills (each with its own rank draw, no duplicates).
2. An `InvocationBufferDto` row is written to `invocation_buffer` (status `PENDING`) *before* any external call — this is the audit/replay record, holding JSON snapshots of the monster and every request/response exchanged.
3. `MonstersApiClient.createMonster(...)` is called; on success the buffer moves to `MONSTER_CREATED` and the returned `monsterId` is recorded.
4. `PlayerApiClient.addMonsterToPlayer(...)` is called; on success the buffer moves to `COMPLETED`.
5. **Compensation**: if step 4 fails after step 3 succeeded, `InvocationService` calls `MonstersApiClient.deleteMonster(createdMonsterId)` to roll back the orphaned monster, marks the buffer `FAILED` with the failure reason, and rethrows `ExternalApiException` (mapped to HTTP 502 by `GlobalExceptionHandler`). If the compensating delete itself fails, that's only logged — there's no automatic retry.

`replayBufferedInvocations()` re-runs `executeInvocation()` for every buffer entry not yet `COMPLETED` (`PENDING`/`MONSTER_CREATED`/`FAILED`), reusing the persisted monster snapshot and request bodies, and returns an `InvocationReplayReport` (success/failure counts, failed IDs). This is how a partially-failed invocation gets fixed without the client re-invoking (which would burn a fresh random roll).

### Auth model — forwards the caller's identity, doesn't hold its own
- `AuthInterceptor` (a `HandlerInterceptor`, not a security filter) guards incoming requests: extracts the bearer token from the `Authorization` header or a `token` cookie, verifies it via `AuthApiClient` → `API_authentification`'s `POST /user/verify-token`, and returns 401 if missing/invalid. `OPTIONS` (CORS preflight) is let through unconditionally. Enforcement is feature-flagged by `app.auth.enabled` in `application.yml` (currently `false` — check before assuming auth is active).
- `BearerTokenRestTemplateInterceptor` does the mirror job on the way out: every outbound `RestTemplate` call to `API_monstres`/`API_joueur` picks the bearer token back up from the current inbound request's context (`RequestContextHolder`) and re-attaches it. Downstream services therefore see the *original player's* identity, not a service credential — this is deliberate (see root `docs/ARCHITECTURE_INTER_API.md`) but means the saga's compensating `deleteMonster()` call also rides on the original caller's token, not an admin credential (relevant if you ever want to restrict monster deletion by ownership — see `TODO.md` item 2).
- `AuthHandler`/`ExternalApiClient`/`AuthApiClient` are this service's own abstraction for talking to `API_authentification`, distinct from (and not yet migrated to) the shared `gatcha-common-security` lib that `API_monstres`/`API_joueur` now use — see `TODO.md` item 1 for the concrete divergences if asked to unify them.

### Data seeding — one JSON file per monster
`DatabaseSeeder` (a `CommandLineRunner`) seeds `monsters`/`skills` from individual JSON files in `src/main/resources/monsters/*.json` (one file per monster, loaded by `MonsterSeedingService` via classpath pattern matching) on first boot, rather than from code or a single fixture file. To add a monster: copy `.TEMPLATE_monster.json`, fill it in, restart. Seeding only runs when the `monsters` table is empty, with a fallback path that seeds skills-only if monsters already exist but skills don't (matching monster JSON files to existing rows by name) — see `docs/MODULAR_DATABASE_SEEDING.md`.

### Package layout
`controller` (+`dto/input`,`dto/output`,`mapper`) → `service` (+`dto`,`mapper`) → `persistence` (repository wrappers) + `persistence/repository` (Spring Data JPA) + `persistence/entity`. `client` holds one class per external API (`MonstersApiClient`, `PlayerApiClient`, `AuthApiClient`, `ApiGenerateGatchaClient`, `ExternalApiClient`) plus `client/dto/{auth,gatcha,monsters,player}` for their wire formats — this mirrors the root CLAUDE.md's note that this service is "the richest" of the four Java services since it's the integration hub. `config/seeding` holds the JSON-seeding DTOs/service described above.

### Known gaps (see `TODO.md` for full detail)
- `config/AuthInterceptor.java` has not been migrated to the shared `gatcha-common-security` lib the other services use, deliberately, pending a full auth review of this service.
- Monster deletion has no ownership check; the saga's compensation path relies on this being open (see above).
