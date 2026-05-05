# RealWorld Backend: Go + Chi (Hexagonal Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Go, the `chi` router, and **Hexagonal Architecture** (also known as Ports and Adapters).

## Architecture

This implementation strictly separates business logic from technical concerns using the Hexagonal pattern:

- **Domain Layer**: The core of the application. Contains pure Go structs (Entities) and business logic. It has zero dependencies on external frameworks or other layers.
- **Application Layer**: Orchestrates domain objects to fulfill use cases.
    - **Ports**: Interfaces defining the input (Service Ports) and output (Repository Ports) requirements.
    - **Application Services**: Implementations of service ports that coordinate business logic and interact with output ports.
- **Infrastructure Layer**: Technical details and library-specific code.
    - **Adapters (In/Out)**: Connect the application to the outside world.
        - **Web (In)**: Chi handlers that parse HTTP requests and format JSON responses.
        - **Persistence (Out)**: Repository implementations using `sqlx` and SQL queries.
    - **Configuration**: Application initialization, dependency injection, and database setup.
    - **Security**: JWT authentication and password hashing (Bcrypt).

## Tech Stack

- **Go 1.26**
- **Chi**: Lightweight and idiomatic router
- **Sqlx**: General purpose extensions to `database/sql`
- **SQLite / PostgreSQL**: Supported via configuration
- **Validator**: Request validation using `go-playground/validator`
- **Httplog**: Structured logging for HTTP requests
- **Testify**: Toolkit for unit and integration testing

## Directory Structure

```text
.
├── cmd/
│   └── realworld/          # Main entry point - Wires dependencies
├── internal/
│   ├── domain/             # Core business models and entities
│   ├── application/        # Use cases and port definitions
│   │   ├── port/           # Service and Repository interfaces
│   │   └── service/        # Business logic implementation
│   └── infrastructure/     # Technical details (Adapters)
│       ├── web/            # HTTP Handlers and DTOs
│       ├── persistence/    # SQL Repository implementations
│       ├── configuration/  # App setup and DB initialization
│       └── security/       # JWT and Auth implementations
├── tests/
│   ├── integration/        # End-to-end API tests
│   └── testmocks/          # Manually maintained mocks for testing
├── go.mod                  # Go module definition
└── README.md
```

## Why this works for Go

Go's **implicit interfaces** are a perfect fit for Hexagonal Architecture. The `application` layer defines the `Repository` interface it requires, and the `infrastructure/persistence` layer satisfies it without ever needing to import the application package. This ensures a clean, one-way dependency flow towards the domain core.

## SQLite Performance & Testing Caveats

For local development and unit/integration tests, this project supports an in-memory SQLite database. To ensure data consistency across the connection pool in SQLite's in-memory mode, `MaxOpenConns` is restricted to `1`.

**Note on Performance:**
- This single-connection restriction impacts performance and concurrency.
- **Load Testing:** Do NOT use the local SQLite setup for load tests or performance comparisons.
- **Environment Parity:** For meaningful performance benchmarks or production-like testing, use the provided Docker setup with a **PostgreSQL** database. This ensures a realistic multi-connection environment comparable to other implementations.
