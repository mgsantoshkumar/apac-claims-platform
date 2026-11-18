# Architecture

## Overview

The MVP is an event-driven claims platform for Chubb APAC motor and property claims.

| Component | Responsibility |
|---|---|
| `frontend` | Angular SPA for claimants and staff (role-based shells) |
| `claims-service` | System of record: auth, claim lifecycle, Redis workload, Kafka producers |
| `notification-service` | Read model: Kafka consumers → claim timeline + in-app notifications |
| `postgres` | Persistence (`claims` and `notifications` schemas) |
| `redis` | Cached workload counters / open reserve |
| `kafka` | Domain event backbone |

## Data ownership

- **claims-service** owns users and claims. It alone mutates claim state.
- **notification-service** owns timeline entries and notifications. It never mutates claims.
- Schemas share one Postgres instance for MVP speed. Split to DB-per-service later without changing event contracts.

## Domain events

| Topic | When |
|---|---|
| `claim.submitted` | Claimant creates a claim |
| `claim.assigned` | Adjuster/manager picks up a claim |
| `claim.info-requested` | Staff asks claimant for more information |
| `claim.info-provided` | Claimant replies |
| `claim.decided` | Claim settled or rejected |

Payload type: `com.chubb.apac.claims.common.events.ClaimDomainEvent` in the `common` module.

## Auth

Demo JWT issued by `claims-service` (`/api/auth/login`). Roles: `CLAIMANT`, `ADJUSTER`, `MANAGER`. Both services verify the same `JWT_SECRET`.

## Cloud parameter map

See [`.env.example`](../.env.example). Key overrides for cloud:

- `KAFKA_ADVERTISED_HOST` — broker DNS/LB hostname reachable by clients
- `CLAIMS_API_PUBLIC_URL` / `NOTIFICATIONS_API_PUBLIC_URL` — browser-reachable API bases baked into the frontend image
- `CORS_ORIGINS` — frontend origin(s)
- `JWT_SECRET` — shared signing secret (rotate in cloud)
- `POSTGRES_*` / Redis hosts — managed services or container hostnames

## Key classes

### claims-service

- `ClaimService` — lifecycle transitions and authorization checks
- `ClaimEventPublisher` — maps event types to Kafka topics
- `WorkloadCacheService` — rebuilds Redis hashes after transitions
- `DataSeeder` — demo users and seed claims
- `JwtService` / `JwtAuthFilter` — demo authentication

### notification-service

- `ClaimEventListener` — `@KafkaListener` on all claim topics
- `ProjectionService` — idempotent timeline + notification writes
- `NotificationController` — timeline and “my notifications” APIs
