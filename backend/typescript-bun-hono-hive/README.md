# RealWorld Backend: TypeScript + Bun + Hono ("Hive" - Vertical Hexagonal)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using TypeScript, Hono, and a **Vertical Hexagonal Architecture**, also known as **Hive**, running on the Bun runtime. It combines the isolation of Hexagonal Architecture with the locality and cohesion of Vertical Slices.

## Architecture (The Hive)

The architecture is divided into three distinct layers, organized by their role in the system:

- **Shared (`src/shared`)**: The foundation. Contains technical primitives (config, database setup, JWT handling, error mapping). It is business-agnostic and depends on nothing but external libraries.
- **Cells (`src/cells`)**: The business features. Each subdirectory represents a self-contained "cell" (a mini-hexagon). A cell contains its own domain, ports (interfaces), and adapters (Controllers, Repositories).
    - *Independence*: Cells act almost like internal microservices.
    - *Strict Communication*: Cells only communicate with each other through defined interfaces (Ports). A cell NEVER imports another cell's database adapter or handler.
- **The Hive (`src/hive`)**: The orchestration layer (Composition Root). It depends on `shared` and all `cells`. It is responsible for instantiating the database, creating all cells, injecting dependencies (satisfying cell ports), and wiring up the final Hono application.

## Tech Stack

- **Bun Runtime** (native TS execution, native HTTP server via `Bun.serve`)
- **Hono**: Ultrafast, web-standards based web framework
- **pg-promise**: PostgreSQL interface for Node.js
- **Zod**: TypeScript-first schema validation
- **Bun.password**: Native Argon2id password hashing
- **Jose**: JSON Web Token (JWT) implementation
- **Bun Test**: Native, high-performance test runner

## Directory Structure

```text
.
├── Dockerfile              # Production Docker build using Bun
├── src/
│   ├── index.ts            # Entry point - Boots the Bun native HTTP server
│   ├── cells/              # --- THE BUSINESS FEATURES (VERTICAL SLICES) ---
│   │   ├── user/           # User & Profile management
│   │   │   ├── user.domain.ts      # Business entities & logic
│   │   │   ├── user.ports.ts       # Interfaces required by this cell
│   │   │   ├── user.service.ts     # Business orchestration
│   │   │   ├── user.repository.ts  # SQL DB Adapter
│   │   │   ├── user.controller.ts  # HTTP REST Adapter
│   │   │   └── user.validator.ts   # Request validation schemas
│   │   ├── article/        # Article & Tag management
│   │   └── comment/        # Comment management
│   ├── hive/               # --- THE WIRING LAYER (COMPOSITION ROOT) ---
│   │   └── app.ts          # Wires cells together, resolves dependencies
│   └── shared/             # --- TECHNICAL PRIMITIVES ---
│       ├── config/         # App configuration
│       ├── database/       # DB initialization
│       ├── errors/         # Global error types
│       ├── security/       # JWT & Password hashing
│       └── web/            # Middleware & HTTP helpers
├── package.json            # Dependencies and scripts
└── README.md
```

## Why this works for TypeScript/Bun/Hono

1.  **Type Safety across Boundaries**: Ports (interfaces) ensure that cells interact predictably, leveraging TypeScript's strong typing without tight coupling.
2.  **Circular Dependency Prevention**: By grouping by business boundary and using ESM, tight coupling becomes obvious, and circular imports are easily caught.
3.  **Scalability**: You can work on the Article feature without ever seeing the User code. It maps perfectly to team ownership in larger organizations.
4.  **Clean Entry Point**: The `index.ts` remains pristine, focusing on infrastructure (HTTP server initialization), while the orchestration logic (`hive`) is easily testable.

## Performance

- Max CPU Utilization: [TBD]%
- Max Memory Usage: [TBD] MiB
- Max Requests per Second: [TBD]

### API test suite

- On startup: [TBD]s
- After 10 warm-up runs: [TBD]s
- After load test: [TBD]s

### Load test suite (TBD)

#### Light load (10 VUs / 30s)

[TBD]

#### Medium load (50 VUs / 1m)

[TBD]

#### Heavy load (200 VUs / 3m)

[TBD]
