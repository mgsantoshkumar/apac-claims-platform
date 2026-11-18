# Architecture Q&A

Answers for the Chubb APAC Claims Platform MVP, grounded in the current codebase.

---

## What are the core entities in this domain? How do they relate?

| Entity | Owner | Role |
|---|---|---|
| **UserAccount** | claims-service | Claimant / adjuster / manager identities |
| **Claim** | claims-service | System of record for the incident and its assessment |
| **ClaimDomainEvent** | Kafka (`common` module) | Immutable facts about claim lifecycle transitions |
| **TimelineEntry** | notification-service | Append-only projection used for claim history in the UI |
| **UserNotification** | notification-service | Per-user notification rows generated from the same events |

**Relationships**

- A **Claimant** owns many **Claims**.
- An **Adjuster** may be assigned to a **Claim**.
- Each lifecycle change emits a **ClaimDomainEvent**.
- Events are projected into **TimelineEntry** records and optional **UserNotification** rows.

**Claim lifecycle**

```text
SUBMITTED → ASSIGNED → (INFO_REQUESTED ↔ UNDER_REVIEW) → SETTLED | REJECTED
```

Markets: `SG`, `HK`, `MY`, `TH`, `VN`, `AU`  
Lines of business: `MOTOR`, `PROPERTY`

---

## Is the backend one service or more than one? What drives that decision?

**Two services:** `claims-service` and `notification-service`.

| Service | Responsibility |
|---|---|
| `claims-service` | Write side / system of record: auth, claim mutations, Redis workload cache, Kafka producers |
| `notification-service` | Read side: Kafka consumers → timeline + in-app notifications |

**Why split**

- Separates transactional claim truth from eventually consistent read models.
- Either side can scale independently.
- Event contracts stay stable even if databases are split later.
- MVP uses one Postgres instance with separate schemas (`claims` / `notifications`) for speed; ownership boundaries remain clear.

---

## What operations need to be synchronous? What can be event-driven?

**Synchronous (REST — must succeed for the user action)**

- Login / JWT issuance
- Create, list, and get claim
- Assign, request info, provide info, decide
- Workload summary read

**Event-driven (async after the write commits)**

- Timeline and notification projection from:
  - `claim.submitted`
  - `claim.assigned`
  - `claim.info-requested`
  - `claim.info-provided`
  - `claim.decided`
- Future fans-outs (email, fraud checks, analytics, etc.)

**Rule of thumb:** anything that changes claim truth is synchronous; anything that informs secondary views can be event-driven.

---

## How does the frontend serve two distinct user types from a single Angular codebase?

One Angular SPA with **role-based shells and route guards**:

1. Login issues a JWT containing role `CLAIMANT`, `ADJUSTER`, or `MANAGER`.
2. `authGuard` and `roleGuard` protect routes.
3. **Claimant shell** (`/claimant`): My claims, report incident, claim detail.
4. **Staff shell** (`/staff`): inbox, claim assessment, workload (adjuster/manager).
5. Shared TypeScript models and API clients; navigation and screens differ by role.

---

## What is the contract between frontend and backend, and who defines it?

**The backend owns the contract.**

- Source of truth: Spring request/response DTOs + springdoc OpenAPI  
  (`http://localhost:18081/swagger-ui.html`, `http://localhost:18082/swagger-ui.html`).
- Frontend mirrors those shapes in TypeScript (`claim.models.ts`, `ClaimsApiService`, `NotificationsApiService`) and environment base URLs.
- There is no generated OpenAPI client yet; the frontend is kept in sync manually with Swagger/DTOs.
- Auth contract: `Authorization: Bearer <JWT>`, verified by both services with the shared `JWT_SECRET`.

---

## How do you handle the async nature of claims processing across both tiers?

1. UI calls **claims-service** → claim is persisted and a Kafka domain event is published.
2. **notification-service** consumes the event → writes timeline/notification projections.
3. UI reads **claim state** from claims-service (immediate) and **timeline** from notification-service (eventual).
4. After load/actions, the UI briefly **polls the timeline** so Kafka lag does not leave an empty history.
5. Redis workload counters are rebuilt on claim transitions for fast staff/manager reads.

---

## What does a claims officer need to see to manage their workload effectively?

From **Inbox** and **Workload**:

- Unassigned queue (`SUBMITTED` claims)
- Their own open assigned cases
- Counts **by status** and **by market**
- Ability to pick up, request information, settle, or reject
- Timeline context on each claim

---

## What does the system need to know about outstanding liability exposure?

**Open reserve exposure** = sum of `reserveAmount` on claims that are **not** `SETTLED` or `REJECTED`.

In this MVP:

- On create, `reserveAmount` is seeded from `estimatedLoss`.
- On settle or reject, reserve is cleared to `0`.
- The total is cached in Redis (`claims:workload:openReserve`) and returned as `openReserveExposure` from `GET /api/claims/workload/summary`.

That figure is the outstanding liability still held against open claims.
