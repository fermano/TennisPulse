# 🎾 Tennis Pulse — Tennis Match Tracking Platform

**Tennis Pulse** is a showcase project built using **Spring Boot**, demonstrating a clean and modern backend architecture for managing tennis players, clubs, and matches.
It was designed to highlight professional development practices: REST API design, layered architecture, Flyway migrations, transactional domain logic, DTO mapping, containerized infrastructure, and future integration with NoSQL, caching, and AWS messaging.

This project is intentionally domain-rich and personality-driven — a tennis-inspired system that goes beyond standard CRUD applications and demonstrates real-world engineering decisions.

---

## 🚀 Features & Functionalities

### 🧍 Players
- Create, list, update and delete players
- (Future) Enriched with analytics data for personalized coaching tips

### 🏟️ Clubs
- Create, list, update and delete clubs
- Store basic metadata like name, city and country

### 🎾 Matches
- Create matches between two players
- Track lifecycle:
  - `SCHEDULED`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `CANCELLED`
- Update match status with winner and final score

### 📊 Analytics & Coaching (MongoDB + Rule Engine)
- Collect per-player and per-match metrics in a NoSQL store (MongoDB)
- Apply a simple rule engine (threshold-based `if` rules) over analytics data
- Expose **coaching tips** based on patterns, e.g.:
  - Low 2nd serve points won
  - High unforced errors on backhand
  - Specific score patterns in tight sets

### 📈 Rankings & Global Stats (Redis)
- Use Redis to cache frequently accessed, global information:
  - Player rankings
  - Leaderboards
  - Hot aggregated stats
- Designed for **fast read access** from the API without hitting PostgreSQL/Mongo every time

### 📬 Asynchronous Events & Integrations (SQS)
- Publish domain events (e.g. `MatchCompleted`) to SQS
- Background workers consume these events to:
  - Update analytics documents in MongoDB
  - Refresh rankings / global stats in Redis
  - Enable future external integrations (notifications, reporting, etc.)



---

## 🧱 Architecture Overview

The project follows a Clean(ish) Hexagonal / Layered Architecture to separate concerns, improve testability, and support multiple infrastructure adapters.

```
┌─────────────────────────┐
│       Controller        │  (REST Layer)
└─────────────────────────┘
           ↓ DTOs
┌──────────────────────────────────────────────┐
│              Application Layer               │
│  - MatchCommandService                       │
│  - MatchQueryService                         │
│  - CoachingService        (uses analytics +  │
│                            rule engine)      │
│  - RankingService         (uses Redis cache) │
└──────────────────────────────────────────────┘
           ↓ business flow
┌─────────────────────────┐
│     Domain Services     │  (business rules for
│   (match logic, etc.)   │   creation/result) 
└─────────────────────────┘
           ↓ persistence (core)
┌───────────────────────────────┐
│      Repository / Infra       │
│                               │
│  ┌─────────────────────────┐  │
│  │   JPA Repositories      │──┼──► PostgreSQL
│  │  (Players, Matches,     │  │   Core source of truth
│  │   Clubs, etc.)          │  │
│  └─────────────────────────┘  │
│                               │
│  ┌─────────────────────────┐  │
│  │   Redis Cache Adapter   │──┼──► Redis
│  │  - Player rankings      │  │   (fast global reads)
│  │  - Global stats         │  │
│  └─────────────────────────┘  │
│                               │
│  ┌─────────────────────────┐  │
│  │    SQS Publisher        │──┼──► SQS
│  │  - MatchCreated         │  │   (domain events)
│  │  - MatchCompleted       │  │
│  └─────────────────────────┘  │
└───────────────────────────────┘

         ▲                                     
         │ SQS events                          
┌──────────────────────────────────────────────┐
│        Analytics / Coaching Side             │
│                                              │
│  ┌───────────────────────────────────────┐   │
│  │          SQS Consumers / Workers      │   │
│  │  - listen to Match events             │   │
│  │  - compute / update analytics         │   │
│  │  - write per-player/per-match stats   │   │
│  └───────────────────────────────────────┘   │
│                  ↓                           │
│  ┌─────────────────────────┐                 │
│  │   Analytics Repository  │──► MongoDB      │
│  │  - per-player metrics   │   (NoSQL        │
│  │  - per-match metrics    │    analytics)   │
│  └─────────────────────────┘                 │
│                  ↓                           │
│  ┌─────────────────────────┐                 │
│  │   Rule Engine           │                 │
│  │  - applies thresholds   │                 │
│  │  - derives coaching     │                 │
│  │    tips from Mongo data │                 │
│  └─────────────────────────┘                 │
│                  ↑                           │
│        CoachingService (Application Layer)   │
└──────────────────────────────────────────────┘

```

### Architectural Choices

- **DTOs** instead of exposing JPA entities  
- **Query Services** with transactional boundaries  
- **Domain Services** enforcing rules (winner/finalScore only for COMPLETED)  
- **Flyway migrations** for reproducible databases  
- **Docker Compose** for local infra  
- **LocalStack** enabling AWS-like messaging

---

## 🐳 Running Tennis Pulse Locally

### Prerequisites  
- Docker + Docker Compose  
- Java 17+  
- Maven or `./mvnw`

---

## 1️⃣ Start infrastructure

```bash
docker compose up -d
```

Starts:
- PostgreSQL  
- MongoDB (future use)  
- Redis (future use)  
- LocalStack (SQS/S3 emulation)

---

## 2️⃣ Run the Spring Boot app

```bash
./mvnw spring-boot:run
```

Flyway migrations run automatically.

App available at:

```
http://localhost:8080
```

---

## 📚 API Endpoints

### Matches
```
GET    /api/matches
GET    /api/matches/{id}
POST   /api/matches
PUT    /api/matches/{id}/status
```

### Players
```
GET    /api/players
POST   /api/players
PUT    /api/players/{id}
DELETE /api/players/{id}
```

### Clubs
```
GET    /api/clubs
POST   /api/clubs
PUT    /api/clubs/{id}
DELETE /api/clubs/{id}
```

---

## 🧪 Postman Collection

Located at:
```
postman/TennisPulse.postman_collection.json
```

---

## 🛠 Tech Stack

- Java 17 / 21  
- Spring Boot 3  
- Spring Web MVC  
- Spring Data JPA / Hibernate  
- Flyway  
- PostgreSQL  
- MongoDB (future analytics)  
- Redis (future caching)  
- LocalStack (AWS emulation)  
- Docker Compose  
- Lombok  

---

## 🔮 Roadmap

- Redis caching  
- MongoDB analytics  
- SQS event on match completed  

---

## 👤 About

**Tennis Pulse** is a personal and technical showcase designed to:  
- Model a real sports domain  
- Demonstrate clean architecture  
- Show production-ready practices  
- Integrate multiple storage and messaging layers

It reflects both engineering capability and personal passion.

