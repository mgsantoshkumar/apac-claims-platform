# Extending the platform

Goal: add modules with minimal configuration and without disturbing existing services.

## Add a new microservice

1. Create `backend/<new-service>/` with its own Spring Boot module and `pom.xml`.
2. Add the module to [`backend/pom.xml`](../backend/pom.xml).
3. Depend on `common` for shared enums/events; **do not** import other services’ JPA entities.
4. Prefer integration via Kafka: consume existing topics and/or publish new topics defined in `common`.
5. Add a Dockerfile under the module and a service entry in [`docker-compose.yml`](../docker-compose.yml).
6. Give the service its own Postgres schema (or database) and env vars in [`.env.example`](../.env.example).

## Add a new domain event

1. Add a topic constant to `ClaimTopics` (or a new `*Topics` class) in `common`.
2. Extend `ClaimEventType` / event payload only if the new event belongs to claims; otherwise introduce a dedicated event record.
3. Publish from the owning service; consume in interested services only.

## Add a frontend feature

1. Create a standalone feature folder under `frontend/src/app/features/<name>/`.
2. Register lazy or eager routes in `app.routes.ts`.
3. Guard by role if needed via `roleGuard([...])`.
4. Call APIs through thin services in `core/` — avoid embedding URLs in components.

## Split databases later

Today both services share one Postgres with schemas `claims` and `notifications`. To split:

1. Provision a second database.
2. Point `notification-service` datasource URL at it.
3. Keep Kafka contracts unchanged — no claims-service code changes required for the split.
