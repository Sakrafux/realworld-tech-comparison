# RealWorld Backend: Go + Chi ("Hive" - Vertical Hexagonal)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Go, the `chi` router, and a **Vertical Hexagonal Architecture**, also known as **Hive**. It combines the isolation of Hexagonal Architecture with the locality and cohesion of Vertical Slices.

## Architectural Idea (The Hive)

The architecture is divided into three distinct layers, organized by their role in the system:

1.  **Shared (`internal/shared`)**: The foundation. Contains technical primitives (logging, database setup, JWT handling, error mapping). It has zero knowledge of business logic.
2.  **Cells (`internal/cells`)**: The business features. Each subdirectory represents a self-contained "cell" (a mini-hexagon). A cell contains its own domain, ports (interfaces), and adapters (HTTP handlers, SQL repositories).
    *   *Independence*: Cells act almost like internal microservices.
    *   *Strict Communication*: Cells only communicate with each other through defined interfaces (Ports). A cell NEVER imports another cell's database adapter or handler.
3.  **The Hive (`internal/hive`)**: The orchestration layer (Composition Root). It depends on `shared` and all `cells`. It is responsible for instantiating the database, creating all cells, injecting dependencies (satisfying cell ports), and wiring up the final HTTP router. 

*Note: In more generic naming schemes, the `cells/` directory is often named `features/`, and the `hive/` orchestration layer is named `app/` or `server/`.*

## Directory Structure

```text
.
├── cmd/
│   └── realworld/
│       └── main.go                 # Very thin entry point, calls the Hive
├── internal/
│   ├── cells/                      # --- THE BUSINESS FEATURES ---
│   │   ├── user/
│   │   │   ├── domain.go           # Business entities & logic
│   │   │   ├── ports.go            # Interfaces required by this cell
│   │   │   ├── service.go          # Business orchestration
│   │   │   ├── repository.go       # SQL DB Adapter
│   │   │   └── handler.go          # HTTP REST Adapter
│   │   │
│   │   ├── article/
│   │   │   └── ... (domain, ports, service, repo, handler)
│   │   │
│   │   └── comment/
│   │       └── ... (domain, ports, service, repo, handler)
│   │
│   ├── hive/                       # --- THE WIRING LAYER (Composition Root) ---
│   │   └── app.go                  # Wires cells together, resolves dependencies
│   │
│   └── shared/                     # --- TECHNICAL PRIMITIVES ---
│       ├── config/
│       ├── database/
│       ├── web/
│       └── security/
├── go.mod
└── README.md
```

## Dependency Flow

`shared` -> `cells` -> `hive` -> `cmd/main`

- **Shared** depends on nothing (only external libraries).
- **Cells** depend on `shared` for utilities, but never on other cells.
- **Hive** depends on `shared` and all `cells` to wire them together.
- **Main** depends only on `hive` to start the application.

## Why this is the "Ultimate Evolution" for Go

1.  **Readable Package Names**: You use `user.Service` instead of `application.UserService`.
2.  **Circular Dependency Proof**: Grouping by business boundary makes tight coupling obvious. If `article` and `user` are too entangled, the compiler prevents cyclical imports.
3.  **Scalability**: You can work on the Article feature without ever seeing the User code. It maps perfectly to team ownership in larger organizations.
4.  **Clean Entry Point**: The `main.go` file remains pristine, and the orchestration logic (`hive`) is easily testable.
