# AI Working Journal

## Entry 1: Understanding the brief
- **Asked:** AI to explain the Chubb assessment and what needs to be built.
- **Got:** Breakdown of the product (claimants, claims staff, managers), deliverables, and what the panel assesses, plus a suggested plan: modular monolith, state machine, role-based auth, workload and exposure endpoints.
- **Accepted:** The overall decomposition and the backend-first priority, since the brief says a strong backend beats a rushed full stack.
- **Reasoning:** Matches my Java/Spring Boot strengths and the brief's own guidance.

## Entry 2: Kafka decision
- **Context:** I have no Kafka experience.
- **Asked:** How Kafka fits, and whether I can leave it out.
- **Got:** Two options: (A) no Kafka, internal Spring events behind a `ClaimEventPublisher` interface, or (B) a small Kafka slice (one topic, one consumer).
- **Decision:** Start with Option A. Add a Kafka implementation later only if time allows.
- **Reasoning:** One service and one DB means Kafka adds operational overhead without benefit at this scope. The interface keeps the door open, and I can explain when Kafka becomes justified (separate consumers, replay, volume across six markets, outbox for reliability).
- **Risk noted:** The panel may ask "why not Kafka?", so I need a prepared answer.

## Entry 3: Environment
- **Problem:** No personal laptop available.
- **Asked:** AI for alternatives and how to approach the recruiter.
- **Got:** Options (cloud dev environment, loaner laptop, extra time) and a draft email.
- **Decision:** Use GitHub Codespaces. I verified the toolchain myself (Java 25, Node 24, Docker 29, Maven 3.10, Docker Compose v5) rather than assuming.
- **Rejected:** Using my employer's laptop, because of policy and IP risk.

## Entry 4: Database choice (challenged the AI)
- **AI suggestion:** PostgreSQL.
- **My pushback:** I only know MSSQL, so I asked why Postgres and what the drawbacks of SQL Server are.
- **Got:** Comparison. SQL Server needs about 2 GB RAM minimum, a 1.5 GB image, slower startup (which slows Testcontainers), and an EULA step. Postgres is lighter in containers and common in Spring Boot tooling. SQL differences are minor, and JPA plus Flyway abstract most of them.
- **Decision:** Postgres, because it keeps the Codespace responsive and keeps tests fast.
- **Follow-up:** Learn the main syntax differences (identity columns, `TIMESTAMPTZ`, `LIMIT`).

## Entry 5: Project scaffold
- **Asked:** AI for the repo setup steps.
- **Got:** Spring Initializr command (web, JPA, validation, security, Postgres, Flyway, actuator, Testcontainers) and a Postgres docker-compose.
- **To verify:** Java version compatibility, and that Postgres starts via `docker compose up -d`.

## Entry 6: Datasource config, Flyway and ddl-auto
- **Asked:** AI to check my config, since I used `application.properties` instead of the YAML it gave me, and to explain `ddl-auto: validate` and Flyway.
- **Got:** The equivalent `.properties` settings, and explanations:
  - Flyway applies versioned SQL migrations and records them in `flyway_schema_history`.
  - `ddl-auto=validate` makes Hibernate only check that entities match the schema, instead of changing it.
- **Accepted:** Flyway as the source of truth for the schema, with `validate` so that drift between entities and tables fails at startup. I rejected `update` because it hides schema changes and can't be reviewed.
- **Kept my own choice:** `.properties` over YAML, since both work the same.
- **Schema design (V1):** `claims` and `claim_status_history` tables, a `version` column for optimistic locking, `NUMERIC(14,2)` for money, `market` and `currency` fields for the six markets, and indexes on status, officer and claimant.

## Entry 7: Postgres not running
- **Problem:** The app failed with "Connection to localhost:5432 refused".
- **Diagnosis:** `docker ps -a` showed no containers, so Postgres had never been started. The app config was fine.
- **Fix:** Ran `docker compose up -d` from the repo root, where `docker-compose.yml` lives.
- **Lesson:** Containers don't persist across a Codespace stop/start. I'll add `docker compose up -d` to the README startup steps.

## Entry 8: Spring Security default lock-out
- **Problem:** The app started but showed a login page for every URL.
- **Cause:** Spring Security is on the classpath and locks everything by default. My temporary open `SecurityConfig` wasn't applied, because its `package` line didn't match the folder, so component scanning never picked it up.
- **Fix:** Corrected the package declaration and restarted. The login page disappeared and endpoints became reachable.
- **Note:** The open config is temporary. I'll replace it with JWT authentication and CLAIMANT / OFFICER / MANAGER roles.
- **Lesson:** Spring Boot only scans the main class's package and its sub-packages.

## Entry 9: First successful startup
- **Result:** The app starts and connects to Postgres, and Flyway runs V1. The Whitelabel 404 at `/` is expected, because no controller is mapped yet.
- **Verified by:** [add this once you've done it: `/actuator/health` shows UP and `\dt` lists `claims`, `claim_status_history`, `flyway_schema_history`].
- **Next:** `ClaimStatus` state machine, `Claim` entity with `@Version`, repository, and `POST /api/claims`.

## Entry 10: First vertical slice (domain, API, error handling)
- **Asked:** AI to start building the core: state machine, entity, repositories, DTOs, service, controller, exception handler, and a first unit test.
- **Got:** `ClaimStatus` enum with allowed transitions, a `Claim` entity (`@Version` for optimistic locking, `BigDecimal` money, market and currency fields), `ClaimStatusHistory` audit entity, request/response records, `ClaimService`, `ClaimController`, and a `@RestControllerAdvice` returning RFC 7807 `ProblemDetail`.
- **Accepted:**
  - Status changes go only through `Claim.changeStatus()`, so the state machine can't be bypassed.
  - DTOs instead of returning entities, so the API contract is separate from the DB model.
  - Submit writes the claim and its first history row in one transaction.
- **Changed/to review:** [add any transitions you changed, e.g. whether REJECTED should be reachable from UNDER_REVIEW].
- **Known shortcut:** `claimantId` comes from the request body for now. It will come from the JWT once auth is added.

## Entry 11: Package name mismatch (AI assumption vs my project)
- **Problem:** The AI's code used the package `com.chubb.claims.claimsservice`, but my generated project uses `com.chubb.claims.claims_service`. VS Code wrote the `package` lines to match the folders, so imports and packages disagreed.
- **Diagnosis:** I spotted the mismatch between the `package` line and an import in the repository file.
- **Fix:** Bulk-replaced the package name with a `sed` command, then recompiled.
- **Lesson:** AI can't see my project layout, so I verify generated package names and paths against the real structure before trusting the code.

## Entry 12: Tests and git workflow slips
- **Unit tests:** `ClaimStatusTest` covers the happy path, skipped stages, terminal states and the entity rejecting an illegal transition. [Add: 4 tests, all passing.]
- **Slip 1:** Ran `./mvnw test` from the repo root, where there is no Maven wrapper. Fixed by running it from `claims-service`.
- **Slip 2:** Ran `git commit` without `git add`, so nothing was staged and the new files stayed untracked. Fixed by staging first.
- **Process:** Using small commits with descriptive messages, marking unverified work as WIP.

## Entry 13: End-to-end verification
- **Result:** `POST /api/claims` returned a claim with status `SUBMITTED`, a generated UUID and claim number `CLM-SG-...`, and createdAt/updatedAt timestamps.
- **Still to confirm:** [tick these off as you do them]
  - `GET /api/claims/{id}` returns 200 with the same claim
  - Random UUID returns 404 with a clear message
  - Bad input (`market: "sg"`, negative amount) returns 400 with field-level errors
  - `claim_status_history` has one row (`null → SUBMITTED`)
- **Next:** Officer side: claim queue, assign/pick-up with optimistic locking, and status-change endpoints that write history rows.

## Entry 14: Officer workflow verified
- **Compile error:** `InfoRequestNotFoundException` had a package line (`service`) that didn't match its folder (`domain`). Fixed by correcting the package line.
- **Surprise 409:** my first pick-up returned 409 because the `$ID` shell variable still pointed to a claim I had already taken through the whole lifecycle (SETTLED, version 6). The API behaved correctly. Lesson: create a fresh claim per test run.
- **Verified:** pick-up by officer-1 succeeds; a second pick-up gets 409; officer-2 assessing officer-1's claim gets 403; settled claims can't transition again; [add the audit trail and concurrency results].

## Entry 15: Audit trail verified; claim queue and dashboards
- **Verified:** history for a full-lifecycle claim shows 7 ordered rows with correct actors (claimant vs officer) and reasons.
- **Asked AI for:** a filterable, paged claim queue; a workload view per officer; a liability exposure view.
- **Accepted:** exposure = reserve if assessed, else claimed amount; open = not REJECTED/SETTLED; grouped by market and currency, never summed across currencies; aggregates done in SQL via JPQL projections.
- **Challenged/changed:** [note anything you changed]
- **Limitation:** endpoints are open until JWT/roles are added.