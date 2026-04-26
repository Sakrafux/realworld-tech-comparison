# RealWorld Backend: Java Micronaut (Hexagonal Architecture)

This is a conceptual layout for a **Hexagonal Architecture** (Ports and Adapters) implementation using Java and Micronaut.

## Architectural Idea
This implementation groups code by its **technical role** to ensure a strict separation between the business core and technical infrastructure. We leverage Micronaut's AOT (Ahead-Of-Time) compilation to provide compile-time safety for dependency injection across the boundaries.

- **Domain Layer**: The core business entities (POJOs) and core exceptions. Zero dependencies on Micronaut or any third-party libraries.
- **Application Layer**: Contains the "What" of the application.
    - **Ports**: Interfaces defining how to interact with the core (Input Ports) and what the core needs from the outside (Output Ports).
    - **Services**: Pure logic implementations that orchestrate domain models.
- **Infrastructure Layer**: The "How" of the application.
    - **Adapters**: Concrete implementations of the ports (Micronaut Data for persistence, REST controllers for the web).
    - **Configuration**: Setup for the Micronaut framework.

## Potential Directory Structure

```text
.
├── src/main/java/com/sakrafux/realworld/
│   ├── domain/             # Pure business models and exceptions
│   ├── application/        # Application logic & port definitions
│   │   ├── port/in         # Driving Ports (Use Cases / Queries)
│   │   ├── port/out        # Driven Ports (Gateways / Repositories)
│   │   └── service/        # Orchestration of domain objects
│   │
│   └── infrastructure/     # Technical implementations
│       ├── adapter/in/web  # REST Controllers & Web DTOs
│       ├── adapter/out/persistence # Micronaut Data & Repository Adapters
│       ├── configuration/  # Micronaut specific setup
│       └── security/       # JWT generation and validation
│
├── pom.xml
└── README.md
```

## Why this works for Micronaut
Micronaut's **Dependency Injection** system is designed for modularity. By defining explicit interfaces (Ports), you can use Micronaut's `@Inject` to wire Adapters without the core logic ever knowing about the framework details. Its high performance and native compatibility make it an ideal choice for the extra layers required by Hexagonal Architecture.
