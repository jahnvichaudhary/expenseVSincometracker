# Finance Tracker

A small full-stack expense and income tracker. Java 17 + Spring Boot 3 on the backend, a single-page vanilla JS frontend bundled in, JWT auth with USER/ADMIN roles, and Docker for one-command deploys.

## Run it locally (no Docker)

Requires Java 17 and Maven 3.9+.

```bash
mvn spring-boot:run
```

Then open: **http://localhost:8080**

Data is persisted to a local H2 file under `./data/`.

## Run with Docker (Postgres)

```bash
docker compose up --build
```

App: **http://localhost:8080**
Postgres: **localhost:5432** (user `tracker`, password `tracker_pw`, db `tracker`)

The first account you register automatically gets the ADMIN role.

## API

All endpoints rooted at `http://localhost:8080`.

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | none | `{ name, email, password }` |
| POST | `/api/auth/login` | none | `{ email, password }` → JWT |
| GET  | `/api/transactions` | USER | list mine |
| POST | `/api/transactions` | USER | `{ type, category, description, amount, date }` |
| DELETE | `/api/transactions/{id}` | USER | own only |
| GET  | `/api/transactions/summary` | USER | totals |
| GET  | `/api/admin/users` | ADMIN | list all users |

Send the token as `Authorization: Bearer <token>`.

## Environment variables

| Var | Default |
|---|---|
| `SPRING_PROFILE` | `dev` |
| `DB_URL` | H2 file at `./data/tracker` |
| `DB_USER` / `DB_PASS` / `DB_DRIVER` | H2 defaults |
| `JWT_SECRET` | dev fallback (rotate in prod) |

## Project layout

```
src/main/java/com/finance/tracker
  config/         Spring Security wiring + CORS
  controller/     REST endpoints
  dto/            request/response records
  entity/         JPA entities
  exception/      typed errors + @RestControllerAdvice
  repository/     Spring Data JPA
  security/       JWT service + filter + UserDetailsService
  service/        business logic
src/main/resources
  application.yml
  static/index.html   single-page UI served by Spring
```

## Deploy

Any host that runs a Docker container works. Common options:

- **Render / Railway / Fly.io** — point at this repo, they'll detect the Dockerfile.
- **A VPS** — `docker compose up -d --build`.
- **Kubernetes** — the multi-stage image is ~200MB and runs as non-root on port 8080.

Remember to set `JWT_SECRET` to a long random string in production.
