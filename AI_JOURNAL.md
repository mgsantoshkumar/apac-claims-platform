# AI Working Journal — Chubb APAC Claims MVP

Running log of prompts, accepted suggestions, challenges, and overrides.

## 2026-11-18 — Kickoff

**Asked:** Build an event-driven claims MVP (Java/Spring Boot + Angular + Kafka) for Chubb APAC motor/property claims, expandable modules, Docker-ready, README + docs, meaningful commits, AI journal; target under ~2 hours.

**Accepted:**
- Two microservices only for MVP: `claims-service` (system of record) + `notification-service` (timeline/projections via Kafka).
- Shared `common` module for events/enums so new modules integrate via contracts.
- Demo JWT auth (CLAIMANT / ADJUSTER / MANAGER) instead of real IdP.
- One Postgres instance with separate schemas (not DB-per-service yet).
- Redis for workload counters.
- Angular Material for operational UI; same SPA for both user types.

**Challenged / overridden:**
- Full six-service mesh, SSO, document storage, Kafka Streams, and K8s Helm deferred — too heavy for MVP and would dilute a working demo.
- Shared DB schemas chosen over DB-per-service for local speed; path to split documented in EXTENDING.md.

**Reasoning:** Ship end-to-end claim lifecycle + Kafka-driven timeline first; keep extension points (topics, modules, lazy Angular features) explicit so later modules do not disturb claims core.
