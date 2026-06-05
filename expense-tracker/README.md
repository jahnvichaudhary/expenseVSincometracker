# Expense & Income Tracker

A self-hosted, lightweight personal finance dashboard designed to bridge the gap between "too simple to be useful" (spreadsheets) and "too complex to bother with" (enterprise accounting software). It provides a fast, single-page application (SPA) to log transactions, monitor cash flow at a glance, and analyze spending patterns with minimal friction.

## 🚀 Problem Statement

Most people lose track of where their money goes. Conventional options present clear pain points:
* **Spreadsheets:** Too manual, prone to breaking formulas, and lack an optimized mobile/desktop entry flow.
* **Accounting Software:** Overkill for personal finance, demanding a steep learning curve and double-entry bookkeeping.
* **Commercial Apps:** Sell your data, lock functionality behind subscriptions, or force cloud-based storage on external servers.

This project addresses these exact challenges through three pillars:
1. **Visibility:** A running list of income vs. expenses makes cash flow tangible. You can't fix what you can't see.
2. **Low Friction:** If logging a transaction takes more than 10 seconds, people stop doing it. The entire user experience is built around rapid entry.
3. **Data Access & Privacy:** Fully self-hosted. Your sensitive financial data stays on your machine, not in a corporate cloud.

---

## 🏗️ Design Decisions & Trade-offs

The architecture follows a strict **keep it simple and self-contained** philosophy. Below is the rationale behind the structural choices and the trade-offs accepted.

### 1. Java + Spring Boot (Backend)
* **Why:** The ecosystem around Spring Security and Spring Data JPA is battle-tested for authentication and structured data persistence. For a CRUD application requiring role-based access control (RBAC), Spring Boot delivers high security with fewer "footguns" than wiring custom JWT middleware by hand in Express or Go.
* **Trade-off:** It has a heavier memory footprint and slower startup time than a Python FastAPI script or Go binary. However, it provides compile-time type safety and a predictable project architecture that scales neatly without falling apart.

### 2. Vanilla JavaScript (Frontend)
* **Why:** The frontend is a single-page dashboard, not a complex social network. Avoiding a build step, `npm`, and an oversized framework (like React or Vue) for a handful of forms and tables prevents overengineering and keeps deployment dead simple.
* **Trade-off:** Manual DOM manipulation will become messy and harder to maintain if feature density scales past the current scope. 

### 3. JWT Auth via HS256 (Stateless)
* **Why:** Stateless authentication removes the need for a server-side session store. HS256 (symmetric signing) is trivial to configure compared to RS256 (asymmetric key pairs) and remains thoroughly secure because the backend application is the sole token issuer and verifier.
* **Trade-off:** Token revocation is inherently difficult. You cannot instantly invalidate a JWT without maintaining an active token deny-list. For a self-hosted or small-team personal finance app where administrative lockouts are rare, this is a reasonable compromise.

### 4. Dual Database Profiles: H2 & PostgreSQL
* **Why:** Running the command `./mvnw spring-boot:run` should work out of the box with zero external configuration—which H2 database profiles provide perfectly for development. In production, a PostgreSQL profile swaps in to ensure multi-user concurrency and absolute data durability.
* **Trade-off:** Supporting two database engines introduces minor syntax and dialect divergence risks. The application schema is kept clean and ANSI SQL-compliant to circumvent this.

### 5. First-User-Becomes-Admin Pattern
* **Why:** Zero-configuration setup. The very first user to register on a clean deployment automatically receives the Administrative role. No SQL initialization scripts to run, no default credentials (`admin/admin`) to leak or forget to change.
* **Trade-off:** If you lose your credentials and you are the lone administrator, there is no automated password reset path. Recovery requires direct database intervention via SQL.

### 6. Single Monolithic Container
* **Why:** The application governs a single unified domain (transactions and authentication). Splitting this footprint into an `auth-service` and a `transaction-service` adds network latency, infrastructure complexity, and distributed transaction headaches for zero tangible benefit.
* **Trade-off:** Auth logic and transaction workflows cannot scale independently. At the current operational scale, this remains a purely theoretical problem.

---

## ⚖️ Known Limitations

Before deploying, be aware of the following scope limitations currently present in the codebase:

* **Authentication Rigidness:** * No built-in password reset flow. Requires manual database entry to override passwords.
  * JWTs cannot be explicitly revoked upon logout. Tokens remain valid for their full lifetime until expiration.
* **Feature Scope:**
  * **No Data Export:** Data is bound to the database. There is no native export mechanism to CSV, PDF, or OFX.
  * **No Recurring Transactions:** Scheduled/recurring entries (e.g., monthly rent, recurring subscriptions) must be logged manually every month.
  * **Single Currency Only:** No exchange rate calculations or multi-currency wallets; financial values are formatted uniformly under a single system currency.
* **Operational Readiness:**
  * **No Automated Backups:** The provided Docker Compose layout does not include scheduled cron backups. If the storage volume fails, data loss occurs. Production instances should wrap `pg_dump` on a cron schedule to an external destination (e.g., AWS S3).
  * **Client-Side Rendering (CSR):** The SPA fetches everything dynamically after the initial script loads. (Though SEO is irrelevant for an internal dashboard, initial load optimizations could be improved later via Server-Side Rendering).
  * **Minimal Test Coverage:** While unit testing structures exist, code coverage is minimal. Production hardening requires integration suites covering concurrent writes and database rollbacks.

---

## 📈 What I'd Do Differently at Scale

If this application were re-architected to support millions of concurrent global users, the following structural changes would be prioritized:

```
[Client / UI (Vite + React + CDN)]
               │
               ▼
   [API Gateway / Keycloak (OIDC Auth)]
               │
               ▼
[Event Bus / Message Queue (Kafka/NATS)] ──► [Async Write Worker] ──► [PostgreSQL Primary (Writes)]
               │                                                                   │
               ▼                                                                   ▼
[Read Optimization Service / CQRS] ◄───────────────────────────────── [Read Replicas (Dashboard Queries)]
```

### 1. Decouple Identity & Access Management (IAM)
Rolling custom JWT logic becomes a security liability at scale. The authentication layer would be migrated to OAuth2/OIDC standards using an identity provider like **Keycloak**, **Authentik**, or a managed service to offload multi-factor authentication (MFA), audit logging, and secure password recovery.

### 2. Event-Driven Architecture & Async Writes
To eliminate database contention under heavy user write stress, an event bus (such as **Apache Kafka**, **NATS**, or **RabbitMQ**) would be placed before the persistence layer. Write mutations would process asynchronously, allowing the API to respond immediately while ensuring eventual consistency.

### 3. Read Replicas & CQRS
Dashboard analytics queries are heavily read-intensive. Implementing **Command Query Responsibility Segregation (CQRS)** would split the database pipeline: a PostgreSQL primary node optimized exclusively for writes, and multiple read-replicas handling real-time dashboard calculations, denormalized views, and materialized aggregations.

### 4. Separate & Modernize the Frontend
Vanilla JS fails to maintain legibility when features expand past a certain threshold. The frontend would be rebuilt inside a framework ecosystem like **Vite + React/Svelte**, leveraging modular component design, centralized state management, and asset deployment over global Content Delivery Networks (CDNs).

### 5. Multi-Stage CI/CD Deployment Pipeline
The deployment pipeline would move away from basic standalone container execution and adopt enterprise deployment patterns:
* **Blue/Green or Canary Deployments** to minimize service interruption.
* **Automated Database Migrations** (via Liquibase or Flyway) featuring declarative rollback logic.
* **Feature Flagging** to toggle high-impact visual components safely without coupling releases to deployments.

### 6. Observability Stack Integration
Standard output logging (`stdout`) and browser console tracking would be swapped for a modern telemetry pipeline: structured logging utilizing **JSON formats**, metrics collection via **Prometheus**, distributed tracing via **OpenTelemetry**, and immediate incident alerting through platforms like **PagerDuty** or **OpsGenie**.
