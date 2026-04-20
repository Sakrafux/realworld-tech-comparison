# RealWorld Backend: Java Spring Boot (Layered Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java and Spring Boot.

## Architecture

This implementation follows a classic **Layered Architecture** (also known as N-Tier Architecture):

- **Web/Controller Layer**: Handles incoming HTTP requests and maps them to service calls.
- **Service Layer**: Contains the business logic and orchestrates data flow.
- **Persistence/Repository Layer**: Manages data access and interaction with the database.
- **Domain Layer**: Defines the core entities and data models.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Data JPA**
- **H2 / PostgreSQL** (depending on configuration)
- **Maven**
- **Monitoring**: Spring Boot Actuator with Micrometer & Prometheus integration
- **Security**: Stateless JWT Authentication

## Directory Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com.sakrafux.realworld/
│   │       ├── configuration/   # Spring configuration (Security, JPA, Filters)
│   │       ├── controller/      # REST API endpoints and Global Exception Handler
│   │       ├── dto/             # Data Transfer Objects (Request/Response)
│   │       ├── entity/          # JPA Database Entities
│   │       ├── exception/       # Custom exceptions 
│   │       ├── mapper/          # MapStruct mappers (DTO <-> Entity)
│   │       ├── repository/      # Spring Data JPA Repositories
│   │       ├── security/        # JWT parsing and authentication filters
│   │       └── service/         # Core business logic
│   └── resources/               # application.yml
└── test/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── controller/      # Integration tests for REST endpoints (*IT.java)
    │       ├── security/        # Unit tests for security utilities (*Test.java)
    │       └── service/         # Unit tests for business logic (*Test.java)
    └── resources/               # application.yml for testing (in-memory H2)
```
