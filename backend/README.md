# Backend Implementations

This directory contains various backend implementations of the [RealWorld API specification](https://docs.realworld.show/specs/backend-specs/introduction/), showcasing different programming languages, frameworks, and architectural patterns.

**[**Spring Boot (Layered)**](./java-springboot-layered/README.md) is the most deliberate implementation**, featuring the most comprehensive documentation and test coverage; it serves as the reference for other implementations.

## Available Implementations

### Java
*   [**Spring Boot (Layered)**](./java-springboot-layered/README.md): Classic N-Tier architecture (Controller-Service-Repository).
*   [**Spring Boot (Vertical Slice)**](./java-springboot-vertical/README.md): Grouped by business feature for high cohesion.
*   [**Spring Boot (Hexagonal)**](./java-springboot-hexagonal/README.md): Strict separation of business logic using Ports and Adapters.
*   [**Spring Boot (Hive)**](./java-springboot-hive/README.md): Vertical Hexagonal architecture, combining feature slices with strict ports.
*   [**Quarkus (Vertical Slice)**](./java-quarkus-vertical/README.md): Optimized for performance and GraalVM, using the Active Record pattern (Panache).
*   [**Micronaut (Hexagonal)**](./java-micronaut-hexagonal/README.md): Leveraging AOT compilation and dependency injection for a clean decoupled core.

### Go
*   [**Go + Chi (Hexagonal)**](./go-chi-hexagonal/README.md): Using Go's implicit interfaces to implement the Ports and Adapters pattern.
*   [**Go + Chi (Hive)**](./go-chi-hive/README.md): Vertical Hexagonal architecture, organizing features into self-contained hexagonal "cells."

### TypeScript
*   [**Express (Vertical Slice)**](./typescript-express-vertical/README.md): High cohesion per feature using TypeScript for type safety across the slice.

### Python
*   [**Django (Vertical Slice)**](./python-django-vertical/README.md): Leveraging standard Django apps with an additional Service Layer for logic isolation.
*   [**FastAPI (Hive)**](./python-fastapi-hive/README.md): Utilizing Python Protocols and FastAPI's DI system for feature-grouped Hexagonal architecture.

## Architectural Patterns Comparison

| Pattern | Focus | Pros | Cons                                           |
| :--- | :--- | :--- |:-----------------------------------------------|
| **Layered** | Technical Role | Simple, intuitive, industry standard. | Logic leakage, hard to scale as project grows. |
| **Vertical Slice** | Business Feature | High cohesion, easy to navigate, scale-friendly. | Potential model redundancy between slices.     |
| **Hexagonal** | Logic Isolation | Pure business logic, tech-agnostic core. | High boilerplate (Mappers, DTOs).              |
| **Hive** | Feature Isolation | Best of both worlds, truly modular, "Microservice-ready." | Complex structure to maintain.                 |
