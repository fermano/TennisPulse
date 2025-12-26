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

## Authentication & Authorization

The API is secured using **OAuth 2.0 / OpenID Connect** with **Keycloak** acting as the Identity Provider (IdP).

### Overview
- Keycloak is run locally via Docker Compose.
- Tennis Pulse acts as a **Resource Server**, validating JWT access tokens issued by Keycloak.
- Users, credentials, and roles are managed entirely by Keycloak.
- The API itself does **not** store passwords.

This setup reflects a modern, production-aligned authentication architecture while remaining simple to run locally.

### Local Development Setup

Keycloak is started automatically as part of `docker-compose.yml` and is preconfigured on first startup via a realm import.

**Keycloak details (dev defaults):**
- Admin console: `http://localhost:8888`
- Realm: `tennispulse`
- Client ID: `tennispulse-api`
- Test user: `tennispulse`
- Test password: `tennispulse`
- Access token lifetime: **30 minutes**

> These credentials are intended for local development only.

### Obtaining an Access Token (Postman)

To authenticate requests, obtain a JWT access token from Keycloak:

**Request**
```
POST http://localhost:8888/realms/tennispulse/protocol/openid-connect/token
```

**Body** (`x-www-form-urlencoded`)
```
grant_type=password
client_id=tennispulse-api
client_secret=tennispulse-secret
username=tennispulse
password=tennispulse
```

**Response**
```json
{
  "access_token": "<JWT>",
  "expires_in": 1800,
  "token_type": "Bearer"
}
```

### Calling the API

Include the token in all API requests:

```
Authorization: Bearer <access_token>
```

All endpoints require authentication.

---

## Tech Stack

- Java 17+
- Spring Boot 3 (Web MVC, Validation, Security, Cache)
- Spring Security (OAuth2 Resource Server, JWT)
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

This starts PostgreSQL, MongoDB, Redis, and Keycloak with a preconfigured realm.

### 2) Run the Spring Boot app
```bash
./mvnw spring-boot:run
```

Application base URL:
- `http://localhost:8080`

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

## API Versioning

The API is currently **not versioned**. This is a deliberate choice as the service is intended as a single-consumer/internal API.

If backward compatibility across multiple clients becomes a requirement, versioning (e.g. `/api/v1`) would be introduced at the HTTP layer.