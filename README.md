# mantenix-notification-service

Mantenix notification microservice: stores the **notifications** sent to users and lets them
mark them as read.

| Item | Value |
|------|-------|
| Port | `8082` |
| Database | PostgreSQL `mantenix_notifications` (own database — ADR-004) |
| Stack | Java 21 · Spring Boot 4 · Maven · Flyway · springdoc-openapi (ADR-003) |
| Owner | Johann Tafur Farfán (design docs by Carlos Mario Cardona) |
| Design docs | [code-sena/mantenix-docs](https://github.com/code-sena/mantenix-docs) — `09-microservices/services/03-notification-service` |

## Run locally

Requirements: JDK 21 or newer and Docker.

```bash
docker compose up -d            # PostgreSQL 17 on localhost:5432
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

| What | URL |
|------|-----|
| Swagger UI | http://localhost:8082/swagger-ui.html |
| Health check | http://localhost:8082/actuator/health |

Unit tests (no database needed): `./mvnw test`

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/notifications` | Create a notification (status `PENDING`, returns `Location`) |
| `GET` | `/notifications?page=&limit=&status=&read=&userId=` | Paginated list, newest first; every filter is optional |
| `GET` | `/notifications/{id}` | Get a notification |
| `PATCH` | `/notifications/{id}` | `{"read": true}` marks it as read, `{"read": false}` as unread |
| `DELETE` | `/notifications/{id}` | Soft delete (`deleted_at`) |

List response: `{"data": [...], "meta": {"page", "limit", "total", "totalPages"}}`.

## Business rules

| Rule | Error |
|------|-------|
| `userId`, `type`, `channel`, `recipient`, `subject` and `message` are required | `400 VALIDATION_ERROR` |
| `page` ≥ 1 and `limit` between 1 and 100 (default 20) | `400 VALIDATION_ERROR` |
| A new notification always starts as `PENDING` and unread | — |
| Marking as read twice keeps the first reading date | — |
| `userId` is a plain UUID from auth-service: no foreign key across databases | — |
| Deleted notifications are not listed and return `404 NOT_FOUND` | — |

## Phase 1 scope

Planned for the next phase (see the service docs in mantenix-docs):

- Consuming the `UserRegistered` event to create the `WELCOME` notification, with idempotency
  by `source_event_id` (DEC-001, DEC-003). The column already exists.
- Actually sending notifications (`PENDING` → `SENT` / `FAILED`); the channel is still to be decided (DEC-004).
- Per-role visibility with `X-User-Id` / `X-User-Role` from the api-gateway.
- The contract lists `/health`; this service exposes the Spring Boot `/actuator/health`.
