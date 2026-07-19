# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This is `API_invocations`, the Spring Boot orchestration hub of the Gatcha game (saga pattern with compensation — see `docs/ARCHITECTURE_INTER_API.md` and `docs/SAGA_PATTERN.md`), checked out as a git submodule of the `GatchaApi` root repo — see the root repo's CLAUDE.md for the cross-service architecture and how the stack is run (root `docker-compose.yaml` only). Build/test with `./mvnw clean package` / `./mvnw test` (single test: `./mvnw test -Dtest=ClassName#methodName`); format with `mvn spotless:apply`, lint with `mvn checkstyle:check`. Flyway owns the schema (`src/main/resources/db/migration`, `ddl-auto: validate`).

## Git workflow (required)

For any piece of work beyond a trivial one-line fix: create a dedicated branch (`feat/...`, `fix/...`, `perf/...`) from the integration branch (`master` here, unless a `dev`/`development` branch exists), commit in atomic steps with conventional-commit messages in French (`feat:`/`fix:`/`perf:`/`docs:` plus a body explaining the why), then merge back with `--no-ff`. Never commit sizeable work directly on the integration branch.
