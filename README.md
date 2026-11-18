# Chubb APAC Claims Platform (MVP)

Event-driven motor and property claims MVP for six APAC markets. Claimants report and track incidents; claims staff assess, request information, and settle or reject; managers see workload and open reserve exposure.

## Stack

- **Frontend:** Angular 19 + Angular Material (single SPA, role-based shells)
- **Backend:** Java 21, Spring Boot 3.3, Maven multi-module
- **Messaging:** Apache Kafka (KRaft)
- **Data:** PostgreSQL (schemas `claims` / `notifications`), Redis (workload cache)
- **API docs:** springdoc OpenAPI / Swagger UI
- **Runtime:** Docker Compose (local and cloud-ready via `.env`)

## Quick start (Docker)

```bash
cp .env.example .env
./scripts/dev-up.sh
# or: docker compose up --build
```

| Surface | URL |
|---|---|
| UI | http://localhost:14200 |
| Claims Swagger | http://localhost:18081/swagger-ui.html |
| Notifications Swagger | http://localhost:18082/swagger-ui.html |

### Demo users (password: `password`)

| Username | Role |
|---|---|
| `claimant1` | CLAIMANT |
| `claimant2` | CLAIMANT |
| `adjuster1` | ADJUSTER |
| `adjuster2` | ADJUSTER |
| `manager1` | MANAGER |

## Local development (services on host)

1. Start infra only: `docker compose up -d postgres redis kafka`
2. Backend (after `mvn -DskipTests install` from `backend/`):
   - `java -jar claims-service/target/claims-service-0.1.0-SNAPSHOT.jar`
   - `java -jar notification-service/target/notification-service-0.1.0-SNAPSHOT.jar`
3. Frontend: `cd frontend && npm install && npx ng serve --port 14200`

Default API ports: claims `18081`, notifications `18082`.

## Repository layout

```text
backend/
  common/                 Shared enums + Kafka event contracts
  claims-service/         Claim lifecycle, JWT auth, Redis, Kafka produce
  notification-service/   Kafka consume → timeline/notifications
frontend/                 Angular SPA
docs/                     Architecture, domain, extension guide
scripts/                  DB init + compose helper
AI_JOURNAL.md             AI working journal
```

## Main flows

1. Claimant signs in → **Report incident** → tracks status/timeline → answers info requests → sees decision.
2. Adjuster signs in → inbox → **Pick up** → request info or **Record decision**.
3. Manager signs in → **Workload** for status/market counts and open reserve.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Domain model](docs/DOMAIN_MODEL.md)
- [Extending modules](docs/EXTENDING.md)
- [AI journal](AI_JOURNAL.md)

## Tests

```bash
cd backend && mvn test
cd frontend && npm test -- --no-watch --browsers=ChromeHeadless
```

## Cloud deployment notes

Compose services already read hosts, ports, secrets, and public API URLs from environment variables (see `.env.example`). Point `KAFKA_ADVERTISED_HOST`, database/Redis endpoints, `JWT_SECRET`, and frontend public API URLs at your cloud values and rebuild the frontend image so API bases are baked in.
