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