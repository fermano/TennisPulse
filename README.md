# 🎾 Tennis Pulse — Tennis Match Tracking Platform

**Tennis Pulse** is a showcase project built using **Spring Boot**, demonstrating a clean and modern backend architecture for managing tennis players, clubs, and matches.
It was designed to highlight professional development practices: REST API design, layered architecture, Flyway migrations, transactional domain logic, DTO mapping, containerized infrastructure, and future integration with NoSQL, caching, and AWS messaging.

This project is intentionally domain-rich and personality-driven — a tennis-inspired system that goes beyond standard CRUD applications and demonstrates real-world engineering decisions.

---

## 🚀 Features & Functionalities

### 🧍 Players
- Create, list, update, delete players  
- `handedness` (LEFT / RIGHT)  
- Soft delete planned  
- Timestamps (`created_at`, `updated_at` planned)

### 🏟️ Clubs
- Create, list, update, delete clubs  
- Contains name, city, country  
- Soft delete planned

### 🎾 Matches
- Create matches between two players  
- Track lifecycle:
  - `SCHEDULED`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `CANCELLED`
- Final score & winner required only when match is **COMPLETED**  
- Automatic timestamps for start/end  
- Lazy-loaded associations solved via **Query Services + DTOs**

---

## 🧱 Architecture Overview

The project follows a Clean(ish) Hexagonal / Layered Architecture to separate concerns, improve testability, and support multiple infrastructure adapters.

```
┌─────────────────────────┐
│       Controller        │  (REST Layer)
└─────────────────────────┘
           ↓ DTOs
┌─────────────────────────┐
│    Application Layer    │  (Query services, mapping)
└─────────────────────────┘
           ↓ business flow
┌─────────────────────────┐
│     Domain Services     │  (Business rules)
└─────────────────────────┘
           ↓ persistence
┌─────────────────────────┐
│       Repositories      │  (Spring Data JPA)
└─────────────────────────┘
           ↓
┌─────────────────────────┐
│       PostgreSQL        │
└─────────────────────────┘
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
postman/tennis-pulse.postman_collection.json
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

## 🔮 Roadmap (Tech Debt)

- Logging  
- AOP aspect for method entry/exit  
- Redis caching  
- MongoDB analytics  
- SQS event on match completed  
- Expand scoring model  

---

## 👤 About

**Tennis Pulse** is a personal and technical showcase designed to:  
- Model a real sports domain  
- Demonstrate clean architecture  
- Show production-ready practices  
- Integrate multiple storage and messaging layers

It reflects both engineering capability and personal passion.

