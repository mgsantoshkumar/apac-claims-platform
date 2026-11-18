# Chubb APAC Claims Platform (MVP)

Event-driven motor and property claims MVP for six APAC markets. Claimants report and track incidents; claims staff assess, request information, and settle or reject; managers see workload and open reserve exposure.

## Stack

- **Frontend:** Angular 19 + Angular Material (single SPA, role-based shells)
- **Backend:** Java 21, Spring Boot 3.3, Maven multi-module
- **Messaging:** Apache Kafka (KRaft)
- **Data:** PostgreSQL (schemas `claims` / `notifications`), Redis (workload cache)
- **API docs:** springdoc OpenAPI / Swagger UI
- **Runtime:** Docker Compose (preferred) or services on the host

## Prerequisites (required software)

Install these before starting the app.

| Software | Version | Purpose |
|---|---|---|
| **Docker Desktop** (or Docker Engine + Compose v2) | Recent stable | Preferred way to run Postgres, Redis, Kafka, APIs, and UI |
| **Git** | Any recent | Clone / pull the repository |

### Additional tools for host-based (non-Docker) development

Only needed if you run services directly on the machine instead of full Compose:

| Software | Version | Purpose |
|---|---|---|
| **JDK** | **21** (Temurin / Microsoft Build / Corretto) | Run Spring Boot services |
| **Apache Maven** | **3.9+** | Build backend modules |
| **Node.js** | **20 LTS or 22 LTS** (includes npm) | Build / serve Angular UI |
| **PostgreSQL** | **16+** | Claims + notifications data |
| **Redis** | **7+** (Windows Redis 3.x works for local MVP) | Workload cache |
| **Apache Kafka** | **3.8+** (KRaft mode) | Claim domain events |

Optional but useful: a REST client (or browser) for Swagger UI.

### Ports used locally

| Service | Default port |
|---|---|
| UI | `14200` |
| Claims API | `18081` |
| Notification API | `18082` |
| PostgreSQL (Compose) | `15432` → container `5432` |
| Redis (Compose) | `16379` → container `6379` |
| Kafka (Compose) | `19092` → broker |

Host-only installs often use Postgres `5432`, Redis `6379`, and Kafka `9092`; override with the env vars in `.env.example` / process environment when starting the JARs.

## Quick start (Docker — recommended)

1. Install **Docker Desktop** and ensure it is running (WSL 2 backend on Windows).
2. From the repo root:

```bash
cp .env.example .env
./scripts/dev-up.sh
# Windows PowerShell / Git Bash alternative:
docker compose up --build -d
```

3. Wait until containers are healthy, then open the UI.

| Surface | URL |
|---|---|
| UI | http://localhost:14200 |
| Claims Swagger | http://localhost:18081/swagger-ui.html |
| Notifications Swagger | http://localhost:18082/swagger-ui.html |

Stop everything: `docker compose down`

### Demo users (password: `password`)

| Username | Role |
|---|---|
| `claimant1` | CLAIMANT |
| `claimant2` | CLAIMANT |
| `adjuster1` | ADJUSTER |
| `adjuster2` | ADJUSTER |
| `manager1` | MANAGER |

## Local development (services on host)

Use this when Docker is unavailable, or when iterating on a single service.

### 1. Start infrastructure

**With Docker (infra only):**

```bash
cp .env.example .env
docker compose up -d postgres redis kafka
```

**Without Docker:** start local PostgreSQL, Redis, and Kafka yourself, then create the DB/schemas:

```sql
-- as a superuser, matching .env.example credentials
CREATE USER claims WITH PASSWORD 'claims';
CREATE DATABASE claims OWNER claims;
\c claims
CREATE SCHEMA IF NOT EXISTS claims;
CREATE SCHEMA IF NOT EXISTS notifications;
GRANT ALL ON SCHEMA claims TO claims;
GRANT ALL ON SCHEMA notifications TO claims;
```

(or run `scripts/init-db.sql` against database `claims`)

Point the APIs at your hosts/ports, for example:

```bash
# PowerShell examples
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/claims?currentSchema=claims"
$env:SPRING_DATASOURCE_USERNAME="claims"
$env:SPRING_DATASOURCE_PASSWORD="claims"
$env:SPRING_DATA_REDIS_HOST="localhost"
$env:SPRING_DATA_REDIS_PORT="6379"
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

Notification service uses the same DB with `currentSchema=notifications` and the same Kafka bootstrap servers (Redis not required).

### 2. Build and run backend

```bash
cd backend
mvn -DskipTests install

java -jar claims-service/target/claims-service-0.1.0-SNAPSHOT.jar
java -jar notification-service/target/notification-service-0.1.0-SNAPSHOT.jar
```

Default listen ports: claims `18081`, notifications `18082`.

### 3. Run frontend

```bash
cd frontend
npm install
npx ng serve --port 14200
```

Open http://localhost:14200

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
- [Architecture Q&A](docs/ARCHITECTURE_QA.md)
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
