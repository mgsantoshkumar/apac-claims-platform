# Domain model

## Actors

| Role | Capabilities |
|---|---|
| Claimant | Report incident, list own claims, respond to info requests, view decision |
| Adjuster | Inbox, assign/pick up, request info, settle/reject, personal queue + workload |
| Manager | All claims visibility, decide, team workload / open reserve exposure |

## Claim lifecycle

```text
SUBMITTED → ASSIGNED → INFO_REQUESTED? → UNDER_REVIEW → SETTLED | REJECTED
```

- Optional `INFO_REQUESTED` loop: staff asks → claimant replies → `UNDER_REVIEW`.
- Terminal states: `SETTLED`, `REJECTED`.

## Markets and LOB

- Markets: `SG`, `HK`, `MY`, `TH`, `VN`, `AU`
- Lines of business: `MOTOR`, `PROPERTY`

## Core entities

### Claim (`claims.claims`)

System of record for an incident report and its assessment.

Important fields: `claimNumber`, `market`, `lob`, `status`, `claimantUserId`, `adjusterUserId`, loss/reserve/settlement amounts, info request/response, decision reason.

### UserAccount (`claims.users`)

Demo directory of claimants and staff. Passwords are BCrypt-hashed; default password for seed users is `password`.

### TimelineEntry (`notifications.timeline_entries`)

Append-only projection of claim domain events for claimant/staff visibility.

### UserNotification (`notifications.user_notifications`)

Per-user notification rows generated from the same events.

## REST surface (claims-service)

| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/login` | Demo JWT |
| POST | `/api/claims` | Create (claimant) |
| GET | `/api/claims` | Role-scoped list |
| GET | `/api/claims/{id}` | Detail |
| POST | `/api/claims/{id}/assign` | Pick up |
| POST | `/api/claims/{id}/request-info` | Staff → claimant |
| POST | `/api/claims/{id}/provide-info` | Claimant reply |
| POST | `/api/claims/{id}/decide` | Settle/reject |
| GET | `/api/claims/workload/summary` | Redis-backed counts |

## REST surface (notification-service)

| Method | Path | Notes |
|---|---|---|
| GET | `/api/claims/{claimId}/timeline` | Ordered events |
| GET | `/api/notifications/me` | Current user notifications |
