# Tennis Pulse — Tennis Match Tracking Platform

Tennis Pulse is a Spring Boot backend showcase for managing tennis players, clubs, and matches, with analytics and cached leaderboard-style read models. The codebase emphasizes a clean, layered architecture; explicit DTO boundaries; reproducible infrastructure; and pragmatic integrations (PostgreSQL, MongoDB, Redis).


---

## Key Features

### Core Domain (PostgreSQL)
- **Players**
  - Create, list, update, delete players
- **Clubs**
  - Create, list, update, delete clubs
- **Matches**
  - Create matches between two players
  - Match lifecycle: `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
  - Update match status with winner and final score

### Analytics & Highlights (MongoDB)
- Aggregates match analytics stored in MongoDB to generate a **Highlights Dashboard**.
- Computes per-player highlight categories (e.g., best serve, best rally, best net play, pressure performance, clean baseline).
- Supports a time window via `TimelineRange`:
  - `ALL_TIME`, `LAST_MONTH`, `LAST_6_MONTHS`, `LAST_12_MONTHS`, `YEAR_TO_DATE`

### Rankings (Redis Cache)
- Cached read models for ranking endpoints (e.g., “top winners current year” / “top winners last month”).
- Redis TTL configured per cache (e.g., `rankings`, `highlights`) with JSON serialization suitable for Java records/DTOs.

---

## Architecture Overview

The project follows a Clean(ish) layered / hexagonal style:
- **API layer**: controllers + DTOs (records)
- **Application layer**: orchestration/services, transactional boundaries
- **Domain layer**: domain rules and invariants
- **Infrastructure layer**: adapters (JPA/PostgreSQL, MongoDB analytics, Redis cache)

---

## Tech Stack

- Java 17+
- Spring Boot 3 (Web MVC, Validation, Cache)
- Spring Data JPA + Hibernate (PostgreSQL)
- Spring Data MongoDB (analytics)
- Spring Data Redis (cache)
- Flyway (schema migrations)
- Docker Compose (local infra)
- Lombok

(LocalStack/SQS may exist in infra and/or roadmap; treat as optional unless explicitly enabled in your local profile.)

---

## Running Locally

### Prerequisites
- Docker + Docker Compose
- Java 17+
- Maven or `./mvnw`

### 1) Start infrastructure
```bash
docker compose up -d
```

The repository’s `docker-compose.yml` starts the backing services required for local development. 

### 2) Run the Spring Boot app
```bash
./mvnw spring-boot:run
```

App default:
- `http://localhost:8080`

---

## API Documentation (Swagger / OpenAPI)

This project is intended to expose an OpenAPI/Swagger UI (commonly via **springdoc-openapi**). If your build includes springdoc, the defaults are typically:

- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui/index.html`

If those endpoints are not available, add springdoc (example for Spring Boot 3 / MVC):

```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.5.0</version>
</dependency>
```

Then restart the application and access `/swagger-ui/index.html`.

---

## HTTP Endpoints (High-level)


### Matches
- `GET /api/matches`
- `GET /api/matches/{id}`
- `POST /api/matches`
- `PUT /api/matches/{id}/status` 

### Players
- `GET /api/players`
- `POST /api/players`
- `PUT /api/players/{id}`
- `DELETE /api/players/{id}` 

### Clubs
- `GET /api/clubs`
- `POST /api/clubs`
- `PUT /api/clubs/{id}`
- `DELETE /api/clubs/{id}` 

### Rankings (cached)
- `GET /api/rankings/wins/current-year?limit={n}`
- `GET /api/rankings/wins/last-month?limit={n}`

### Highlights (cached)
- `GET /api/analytics/highlights?range={TimelineRange}`

---
