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
*   [**Micronaut (Vertical Slice)**](./java-micronaut-vertical/README.md): Grouped by business feature for high cohesion.

### Go
*   [**Go + Chi (Hexagonal)**](./go-chi-hexagonal/README.md): Using Go's implicit interfaces to implement the Ports and Adapters pattern.
*   [**Go + Chi (Hive)**](./go-chi-hive/README.md): Vertical Hexagonal architecture, organizing features into self-contained hexagonal "cells."

### TypeScript
*   [**Express (Hive)**](typescript-node-express-hive/README.md): High cohesion per feature using TypeScript for type safety across the slice.

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

# Impressions

## Impressions of implementations

### Java - Spring Boot
- The standard Java framework.
- Lots of default behavior, but easy entrypoints to adjust it.
- Fantastic documentation and a big community means there is rarely no answer to any issue.
- Great developer tools and experience.
- Works well enough for small and medium loads, but suffers at higher levels.
- The sheer number of features and extensions makes it feel somewhat bloated.
- **Overall, feels comfortable and safe to use.**
  - Would use again if performance is not critical.

### Java - Quarkus
- Somewhat familiar due to reliance on Jakarta.
- Too many blackboxes that are hard to debug and hard to trace due to less documentation and smaller community.
- Most dependencies need a Quarkus wrapper to properly work.
- Build-time properties are very confusing and require constant double-checking of the documentation. Not intuitive at all.
- GraalVM as first-class citizen.
- Fantastic performance both in JVM and GraalVM, both for singular requests and high load.
- JVM implementation may break under high load!
- **Overall, feels very frustrating due to it being very much a blackbox with many Quarkus dependency working very different from**
**established (Spring Boot) ways of doing things, as well as very fragile and hard to debug.**
  - Would not use again.

### Java - Micronaut
- Very similar style to Spring Boot, which makes it very familiar and comfortable to work with. 
- Some implementation patterns differ somewhat, but very intuitive overall. 
- Fantastic throughput performance, though not that amazing for singular requests.
- GraalVM as first-class citizen.
- **Overall, easy switch due to familiar patterns, easy interoperability, and feels like the natural evolution of Spring Boot**
**without the baggage.**
  - Would use again as an alternative to Spring Boot.

### Go - Chi
- Go is very much "batteries included", which means less need for dependencies.
- Chi as a minimal router framework on top of the built-in router for a better developer experience without deviating much from the core.
- Few language tools force simpler code, which is thus easy to understand.
  - Can be somewhat boring, but boring is usually better for developing stable software.
  - Feels similar to C in style but without having to do everything manually (strings, memory, etc.).
- Necessarily exhaustive error handling is both a blessing and a curse.
  - You are never blindsided by some error. If some error is not handled properly, it's on you.
  - There are lots and lots of possible errors that all need to be handled, which can become tedious.
- No blackboxes at all. You need to wire up everything yourself, which also means you understand everything that does or could happen.
- The directory package system, lots of verbose code (largely due to error handling), and same-directory tests means that proper code architecture is of utmost importance.
- **Overall, the lack of magic makes it feel a bit more cumbersome to use, but also much more confident in the stability of the applicaiton.**
  - Would use again for most use cases. 

## Impression of architectures

### Layered
- The classic.
- Great for small projects with few features, as separating the files over their respective layers is easy to understand and clear.
- Scales horribly for larger projects, as single features are split over the entire codebase, which becomes hard to navigate.
- Natural fit for Java and other languages that have loose import rules and rely on ORM entity objects.
  - Horrible fit for Go, where packages are imported as a whole and strong import rules. Feels like fighting against the language.

### Vertical Slice
- Also known as feature-based architecture.
- Separates code by feature first, leading to small, self-contained packages.
  - All the code lives in one place, which makes navigation and changes much easier as the project scales.
- Can be combined with Layered architecture, by applying the layers in the separate slices as they grow in size
- Slices should be coupled very loosely with no circular dependencies.
- Easily applicable to all languages.

### Hexagonal
- Enforces proper definition of APIs and contracts.
- Leads to more boilerplate code.
- Can be very hit-or-miss depending on languages.
  - Very natural fit for Go and its approach to interfaces. Barely any additional boilerplate, simply a sensible approach to ordering the code.
  - Horrible fit for Java, especially when using ORM entities, since either we need to separate them from the domain entities or violate hexagonal principles. Additionally, "one class, one file" leads to many more files.
- Pure hexagonal architecture scales somewhat similarly bad as layered, since it distributes the code of single features across the codebase.

### Hive / Vertical Hexagonal
- Combination of Vertical Slice and Hexagonal principles.
- Small hexagons separated by feature.
- Hexagonal's emphasis on ports is a natural fit for proper decoupling of features with communication over established ports.
- The smaller size makes navigation and changes easier.
- Natural choice for languages that already work well with Hexagonal architecture.

## What would I choose in the future?

The implementation most persuasive in terms of both developer experience and performance was _Go_ with _Chi_ using a _Hive_ architecture.
It is easy to understand, both individual code and the codebase itself, scales well with additional features, contains no surprises, and performs exceedingly well.

However, _Java_ with _Micronaut_ using a _Vertical_ architecture is well worth mentioning.
It has a similarly good developer experience and style to Spring Boot, but simply performs significantly better.