# RealWorld Backend: Go + Chi (Hexagonal Architecture)

This is a conceptual layout for a **Hexagonal Architecture** (Ports and Adapters) implementation using Go and the `go-chi` router.

## Architectural Idea
This structure groups code by its **technical role** in the application. It creates a strict separation between the core logic and the outside world.

- **Domain**: Pure business logic and data structures. No dependencies on SQL, JSON, or any library.
- **Application (Ports)**: Defines the "In" (Use Cases) and "Out" (Repositories/SPI) interfaces. This is the API of your application core.
- **Infrastructure (Adapters)**: Technical implementation details. Contains the database drivers, HTTP handlers, and third-party integrations.

## Potential Directory Structure

```text
.
├── cmd/
│   └── api/                # Main entry point (main.go) - Wires dependencies
├── internal/
│   ├── domain/             # Business entities (User, Article) and core rules
│   │   ├── user.go
│   │   ├── article.go
│   │   └── errors.go       # Sentinel errors for business logic
│   ├── application/        # Use Cases and Port definitions
│   │   ├── user_port.go    # interface UserService / interface UserRepository
│   │   ├── user_service.go # Implementation of the business logic
│   │   ├── article_port.go
│   │   └── article_service.go
│   └── infrastructure/     # Adapters (External world)
│       ├── persistence/    # Driven Adapters (PostgreSQL, Redis)
│       │   ├── postgres_user.go
│       │   └── postgres_article.go
│       ├── web/            # Driving Adapters (Chi handlers)
│       │   ├── router.go   # Router setup
│       │   ├── user_h.go   # Request/Response mapping to ports
│       │   └── article_h.go
│       └── security/       # JWT and Password hashing implementation
├── go.mod
└── README.md
```

## Most Detailed Directory Structure

```text
.
├── cmd/
│   └── realworld/          # Main entry point (main.go) - Wires dependencies
├── internal/
│   ├── application/        # Use Cases and Port definitions
│   │   ├── port/           # Inbound and Outbound Port interfaces
│   │   │   ├── in/
│   │   │   └── out/
│   │   └── service/        # Implementation of the business logic (Use Cases)
│   ├── domain/             # Business entities and core rules
│   │   └── model/          # Domain models (User, Article)
│   └── infrastructure/     # Adapters (External world)
│       ├── adapter/        # Technical implementation of ports
│       │   ├── in/
│       │   │   └── web/    # Driving Adapters (HTTP handlers)
│       │   └── out/
│       │       └── persistence/ # Driven Adapters (Databases)
│       ├── configuration/  # Setup and dependency injection
│       └── security/       # JWT and security concerns
├── go.mod
└── README.md
```

## Why this works for Go
Go's **implicit interfaces** make this very powerful. The `application` package defines the `UserRepository` it needs, and the `infrastructure/persistence` package satisfies it without ever having to import the application package.

## SQLite Performance & Testing Caveats
For local development and unit/integration tests, this project supports an in-memory SQLite database. To ensure data consistency across the connection pool in SQLite's in-memory mode, `MaxOpenConns` is restricted to `1`. 

**Note on Performance:**
- This single-connection restriction impacts performance and concurrency. 
- **Load Testing:** Do NOT use the local SQLite setup for load tests or performance comparisons. 
- **Environment Parity:** For meaningful performance benchmarks or production-like testing, use the provided Docker setup with a **PostgreSQL** database. This ensures a realistic multi-connection environment comparable to other implementations.
