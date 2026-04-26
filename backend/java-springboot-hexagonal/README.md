# RealWorld Backend: Java Spring Boot (Hexagonal Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java, Spring Boot, and **Hexagonal Architecture** (also known as Ports and Adapters).

## Architecture

This implementation strictly separates business logic from technical concerns using the Hexagonal pattern:

- **Domain Layer**: The core of the application. Contains pure Java POJOs (Models) and business logic. It has zero dependencies on external frameworks or other layers.
- **Application Layer**: Orchestrates domain objects to fulfill use cases.
    - **Input Ports (Use Cases/Queries)**: Interfaces defining what the application can do.
    - **Output Ports**: Interfaces defining what the application needs (e.g., persistence, security).
    - **Application Services**: Implementations of input ports that coordinate business logic.
- **Infrastructure Layer**: Technical details and framework-specific code.
    - **Adapters (In/Out)**: Connect the application to the outside world (REST controllers, JPA repositories, security providers).
    - **Mappers**: Translate data between Domain Models, Web DTOs, and JPA Entities.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Data JPA**
- **H2 / PostgreSQL** (depending on configuration)
- **Maven**
- **MapStruct**: For type-safe mapping between layers
- **Monitoring**: Spring Boot Actuator with Micrometer & Prometheus integration
- **Security**: Stateless JWT Authentication

## Directory Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com.sakrafux.realworld/
│   │       ├── domain/             # Core business models and exceptions
│   │       ├── application/        # Use cases, queries, and orchestration
│   │       │   ├── port/in         # Driving Ports (API for the application)
│   │       │   ├── port/out        # Driven Ports (SPI for infrastructure)
│   │       │   └── service         # Orchestration logic
│   │       └── infrastructure/     # Technical details
│   │           ├── adapter/in/web  # REST Controllers and Web DTOs
│   │           ├── adapter/out/persistence # JPA Entities and Repositories
│   │           ├── configuration   # Spring framework setup
│   │           └── security        # JWT and Auth implementations
│   └── resources/                  # application.yml
└── test/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── domain/             # Logic tests for business models
    │       ├── application/        # Use Case orchestration tests (Mocking ports)
    │       └── infrastructure/     # Adapter and Mapper tests
    └── resources/                  # application.yml for testing
```

## Performance

### API test suite

- On startup: TBD
- After 10 warm-up runs: TBD
- After load test: TBD

### Load test suite

#### Light load (10 VUs / 30s)
*TBD*

#### Medium load (50 VUs / 1m)
*TBD*

#### Heavy load (200 VUs / 3m)
*TBD*
