Expense & Income Tracker
Problem — what real problem does this solve
Most people lose track of where their money goes. Spreadsheets are too manual, and full-blown accounting software is overkill for personal finance. This tracker solves the gap between "too simple to be useful" and "too complex to bother with." It gives users a single place to log transactions, see their cash flow at a glance, and understand spending patterns without exporting CSVs or learning double-entry bookkeeping.

Real problems addressed:

Visibility: You can't fix what you can't see. A running list of income vs expenses makes cash flow tangible.
Friction: If logging a transaction takes more than 10 seconds, people stop doing it. The UI is built around speed.
Access: It's self-hosted. Your financial data stays on your machine, not in some app's cloud.
Design decisions — why this approach over alternatives
1. Java + Spring Boot instead of Node/Python/Go
Why: The ecosystem around Spring Security and JPA is battle-tested for auth and data persistence. For a CRUD app with role-based access control, Spring Boot gets you there with fewer footguns than wiring JWT middleware by hand in Express.
Trade-off: It's heavier than a Python FastAPI script. But it also gives you type safety, compile-time checks, and a clear project structure that won't fall apart when the codebase grows past a few files.
2. Vanilla JS frontend instead of React/Vue
Why: This is a single-page dashboard, not a social network. Adding a build step, npm, and a 100KB framework for a few forms and a table is overengineering.
Trade-off: Manual DOM manipulation gets messy if you add 10 more features. For the current scope, it keeps deployment dead simple — no build, no bundler, just static files served by Spring Boot.
3. JWT with HS256 instead of session cookies or RS256
Why: Stateless auth means no server-side session store. HS256 is simpler to configure than RS256 (no key pairs to manage) and secure enough when the server is the only verifier.
Trade-off: Token revocation is hard. You can't instantly invalidate a JWT without maintaining a deny-list. For a personal finance app where users rarely need admin lockouts, this is acceptable.
4. H2 for dev, PostgreSQL for prod via profiles
Why: spring-boot:run should work out of the box with zero config. H2 gives you that. In production, PostgreSQL gives you durability and concurrent access.
Trade-off: Two databases means slight divergence in behavior (H2 syntax quirks). The schema stays simple enough that this hasn't been an issue.
5. First-user-becomes-admin instead of a seeded admin
Why: Zero setup. You register once and you're the admin. No SQL scripts to run, no default credentials to leak.
Trade-off: If you forget your password and you're the only admin, there's no reset path without database access. This is acceptable for a single-user or small-team deployment.
6. Single monolithic container instead of microservices
Why: The app is one domain (transactions + auth). Splitting this into "auth-service" and "transaction-service" adds network latency, deployment complexity, and distributed transaction headaches for zero benefit.
Trade-off: Can't scale auth and transactions independently. At current scale, that's a theoretical problem, not a real one.
What I'd do differently at scale
1. Split auth into an identity provider
At scale, rolling your own JWT logic is a liability. I'd move to OAuth2/OIDC with a provider like Keycloak, Authentik, or a managed service. This offloads token management, MFA, password resets, and audit logging.

2. Event-driven instead of synchronous writes
If multiple users hit the app simultaneously, database contention becomes real. I'd introduce an event bus (RabbitMQ, NATS, or Kafka) so writes are async and the API responds immediately. The read model (dashboard) stays fast because it's decoupled from the write path.

3. Read replicas and CQRS
Dashboard queries are read-heavy. I'd split reads and writes: PostgreSQL primary for writes, read replicas for the dashboard. This also lets you optimize the read schema separately (denormalized views, materialized aggregations).

4. Separate frontend build
Vanilla JS doesn't scale to 20+ features. At that point I'd add Vite + React (or Vue/Svelte) with proper component architecture, state management, and a separate static hosting layer (CDN).

5. Multi-stage deployment pipeline
Right now it's "build a Docker image and run it." At scale I'd want:

Blue/green or canary deployments
Automated DB migrations with rollback scripts
Feature flags so new UI doesn't ship to everyone at once
6. Monitoring and observability
Currently: logs to stdout, errors to the console. At scale: structured logging (JSON), metrics (Prometheus), tracing (OpenTelemetry), and alerts (PagerDuty/OpsGenie). You can't debug what you can't see.

Known limitations
No password reset flow — If you forget your password and you're the only admin, you need database access to recover. This is fine for a self-hosted single-user setup but blocks real multi-user adoption.

No export functionality — Your data is trapped in the database. There's no CSV, PDF, or OFX export. If you want to migrate to another tool, you write SQL.

No recurring transactions — You have to log rent, subscriptions, and salary manually every month. A production finance app needs scheduled/recurring entries.

No multi-currency support — Everything is treated as a single currency. No exchange rates, no currency symbols beyond basic formatting.

JWTs can't be revoked — Once issued, a token is valid until expiry. There's no logout endpoint that invalidates the token globally. A compromised token is usable for its full lifetime.

No tests — The project has unit tests in structure but coverage is minimal. Real production code needs integration tests for the auth flow, DB rollbacks, and edge cases around concurrent writes.

Client-side rendering only — The SPA fetches everything after page load. SEO is irrelevant for a dashboard, but initial load time could be improved with server-side rendering or static generation of the shell.

No backup strategy — The Docker Compose setup doesn't include automated backups. If the volume dies, your data dies with it. PostgreSQL needs pg_dump on a schedule, ideally to S3 or another offsite target.
