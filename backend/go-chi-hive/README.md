# RealWorld Backend: Go + Chi ("Hive" - Vertical Hexagonal)

This is a conceptual layout for a **Vertical Hexagonal Architecture**, also known as **Hive**. It combines the isolation of Hexagonal Architecture with the locality and cohesion of Vertical Slices.

## Architectural Idea (The Hive)
Each directory in `internal/` represents a self-contained **Cell**. Each cell is a mini-hexagon that contains its own domain, ports, and adapters.

- **Feature-Centric**: Everything related to "Articles" (logic, DB, HTTP) lives in one folder.
- **Independence**: Cells are designed to be almost like internal microservices.
- **Strict Communication**: Cells only talk to each other through defined interfaces in the `port` section, never by touching another cell's database adapter.

## Potential Directory Structure

```text
.
├── cmd/
│   └── api/                # wires all cells together
├── internal/
│   ├── user/               # --- USER CELL ---
│   │   ├── domain.go       # Business entity
│   │   ├── ports.go        # Port interfaces for User
│   │   ├── service.go      # Business orchestration
│   │   ├── repository.go   # DB Adapter
│   │   └── handler.go      # Chi/REST Adapter
│   │
│   ├── article/            # --- ARTICLE CELL ---
│   │   ├── domain.go       # Might only contain what articles need
│   │   ├── ports.go
│   │   ├── service.go
│   │   ├── repository.go
│   │   └── handler.go
│   │
│   ├── shared/             # Shared primitives (Logger, Context Keys, Base Errors)
│   └── config/             # Environment and global configuration
├── go.mod
└── README.md
```

## Why this is the "Ultimate Evolution" for Go
1. **Readable Package Names**: You use `user.Service` instead of `application.UserService`.
2. **Circular Dependency Proof**: If two features are too tightly coupled, it's immediately obvious because you've grouped them by business boundary.
3. **Scalability**: You can work on the Article feature without ever seeing the User code. It maps perfectly to team ownership in larger organizations.
