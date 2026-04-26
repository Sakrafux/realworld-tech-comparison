# RealWorld Backend: Java Spring Boot ("Hive" - Vertical Hexagonal)

This is a conceptual layout for a **Vertical Hexagonal Architecture**, also known as **Hive**, implemented with Java and Spring Boot. It merges the strict isolation of Hexagonal Architecture with the high cohesion of Vertical Slice Architecture.

## Architectural Idea (The Hive)
In the Hive, the top-level package organization is by **Business Feature** (e.g., `user`, `article`). Each feature package is a self-contained "Hexagon" (or Cell) that contains its own Domain, Application, and Infrastructure layers.

- **Feature-Isolated**: Each package (cell) is responsible for its own business logic, API endpoints, and persistence logic.
- **Port-Based Communication**: Cells are forbidden from touching each other's database entities or repositories. They must interact only via **Input Ports** (Services/Use Cases) of other cells.
- **Encapsulation**: Infrastructure details (like JPA Entities or Web DTOs) are kept private within the feature package whenever possible.

## Potential Directory Structure

```text
src/main/java/com/sakrafux/realworld/
├── article/                # --- ARTICLE CELL ---
│   ├── domain/             # Article Models (POJOs)
│   ├── application/        # Article Use Cases & Ports
│   │   ├── port/in         # Input Ports
│   │   ├── port/out        # Output Ports (e.g., ArticleRepository Port)
│   │   └── service/        # Orchestration logic
│   └── infrastructure/     # Article Adapters
│       ├── web/            # REST Controllers & Web DTOs
│       └── persistence/    # JPA Entities & Repository Adapters
│
├── user/                   # --- USER CELL ---
│   ├── domain/
│   ├── application/
│   └── infrastructure/
│
├── core/                   # Shared Infrastructure (Cross-cutting)
│   ├── configuration/      # Global Spring Setup
│   ├── exception/          # Global Exception Handling
│   └── security/           # JWT & Auth Filters
│
└── RealworldApplication.java
```

## Why this is powerful for Spring Boot
1. **Solves the "Big Project" problem**: Large layered projects become "Spaghetti" where a change in a `User` entity breaks 10 unrelated services. In the Hive, changes are localized.
2. **Clear Boundaries**: Using package-private visibility in Java, you can ensure that `ArticleEntity` is never used by the `User` service, forcing developers to use the proper domain interfaces.
3. **Optimized for Teams**: Different teams can own different feature packages (Cells) with minimal merge conflicts and clear ownership.
