# Backend Implementations

This directory contains various backend implementations of the [RealWorld API specification](https://docs.realworld.show/specs/backend-specs/introduction/), showcasing different programming languages, frameworks, and architectural patterns.

The [**Spring Boot (Layered)**](./java-springboot-layered/README.md) implementation serves as the reference, featuring the most comprehensive documentation and test coverage.

## Architectural Patterns

For a broader discussion of where these patterns sit within the hierarchy of architectural decisions, see the [Architecture Discussion](../README.md#architecture-discussion) in the root README.

| Pattern | Focus | Strengths | Trade-offs |
| :--- | :--- | :--- | :--- |
| **Layered** | Technical role | Simple, intuitive, industry standard | Logic leakage across layers; hard to navigate at scale |
| **Vertical Slice** | Business feature | High cohesion; easy to navigate and scale | Potential model redundancy between slices |
| **Hexagonal** | Logic isolation | Pure domain logic; tech-agnostic core | High boilerplate (mappers, DTOs); scattered feature code |
| **Hive** | Feature isolation | Modular; microservice-ready; combines slice cohesion with port isolation | Complex structure to maintain |

## Available Implementations

### Java

| Implementation | Architecture | Key Tech | Link |
| :--- | :--- | :--- | :--- |
| Spring Boot (Layered) | N-Tier (Controller-Service-Repository-Domain) | Java 25, Spring Boot 4.0.5, Spring Data JPA, Maven | [README](./java-springboot-layered/README.md) |
| Spring Boot (Vertical Slice) | Package-by-Feature | Java 25, Spring Boot 4.0.5, Spring Data JPA, Maven | [README](./java-springboot-vertical/README.md) |
| Spring Boot (Hexagonal) | Ports and Adapters | Java 25, Spring Boot 4.0.5, Spring Data JPA, MapStruct, Maven | [README](./java-springboot-hexagonal/README.md) |
| Spring Boot (Hive) | Vertical Hexagonal | Java 25, Spring Boot 4.0.5, Spring Data JPA, MapStruct, Maven | [README](./java-springboot-hive/README.md) |
| Quarkus (Vertical Slice) | Package-by-Feature with Active Record | Java 25, Quarkus 3.35.2, Hibernate Panache, GraalVM, Maven | [README](./java-quarkus-vertical/README.md) |
| Micronaut (Vertical Slice) | Package-by-Feature | Java 25, Micronaut 4.10.14, Micronaut Data (JPA), GraalVM, Maven | [README](./java-micronaut-vertical/README.md) |

### Go

| Implementation | Architecture | Key Tech | Link |
| :--- | :--- | :--- | :--- |
| Go + Chi (Hexagonal) | Ports and Adapters | Go 1.26, Chi, Sqlx, SQLite/PostgreSQL | [README](./go-chi-hexagonal/README.md) |
| Go + Chi (Hive) | Vertical Hexagonal | Go 1.26, Chi, Sqlx, SQLite/PostgreSQL | [README](./go-chi-hive/README.md) |

### TypeScript

| Implementation | Architecture | Key Tech | Link |
| :--- | :--- | :--- | :--- |
| Node.js + Express (Hive) | Vertical Hexagonal | Node.js 24+, Express, pg-promise, Zod, Argon2, Jose, Vitest | [README](./typescript-node-express-hive/README.md) |
| Bun + Hono (Hive) | Vertical Hexagonal | Bun Runtime, Hono, pg-promise, Zod, Bun.password, Jose, Bun Test | [README](./typescript-bun-hono-hive/README.md) |

### Python

| Implementation | Architecture | Key Tech | Link |
| :--- | :--- | :--- | :--- |
| FastAPI (Vertical Slice) | Package-by-Feature | Python 3.13+, FastAPI, Pydantic, asyncpg, bcrypt, PyJWT | [README](./python-fastapi-vertical/README.md) |

## Evaluation

### Technology Impressions

#### Java - Spring Boot
Spring Boot provides a mature, well-documented framework with sensible defaults and a large ecosystem. The developer experience is strong, with excellent tooling, extensive community support, and straightforward customization. Performance is adequate for low-to-moderate workloads but degrades under sustained high concurrency due to JVM overhead and framework weight. The breadth of features can feel excessive for smaller projects.

**Verdict:** A reliable, comfortable choice where performance is not the primary constraint.

#### Java - Quarkus
Quarkus builds on Jakarta EE familiarity but introduces significant friction: many components operate as black boxes, debugging is difficult due to limited documentation and a smaller community, and most dependencies require Quarkus-specific wrappers. Build-time configuration is unintuitive and requires frequent reference to documentation. GraalVM support is first-class, and both JVM and native performance are impressive on individual requests. However, the JVM mode exhibited instability under heavy load in testing.

**Verdict:** Impressive throughput, but the developer experience suffers from poor transparency and fragile tooling. Not recommended for production use without significant familiarity with the framework.

#### Java - Micronaut
Micronaut mirrors Spring Boot's programming model, making adoption straightforward for developers with Spring experience. Some implementation patterns differ, but the overall approach remains intuitive. Throughput performance is notably strong, though individual request latency is less competitive. GraalVM is a first-class target.

**Verdict:** A compelling alternative to Spring Boot with similar ergonomics, better throughput, and less legacy baggage.

#### Go - Chi
Go's standard library provides much of what a backend service needs, reducing external dependencies. The Chi router adds a thin ergonomic layer over the built-in HTTP machinery without obscuring it. The language's simplicity — static typing, explicit error handling, no inheritance — produces code that is easy to read and reason about, if sometimes verbose. There are no implicit frameworks or "magic"; everything is wired explicitly. The package system and mandatory error handling demand disciplined architecture, but reward it with confidence in correctness.

**Verdict:** Excellent stability and performance with minimal abstraction overhead. A strong choice for most backend services where straightforward, maintainable code is valued.

#### TypeScript - Node.js - Express
The Node.js ecosystem offers overwhelming choice in libraries and patterns, which is both a strength and a risk. TypeScript provides useful type safety, but the burden of selecting and consistently applying architectural patterns falls entirely on the team. Performance is adequate but not competitive with compiled or natively concurrent runtimes. The lack of opinionated defaults means projects can easily accumulate inconsistent approaches.

**Verdict:** Suitable for small-to-medium services with well-defined dependency choices. Requires significant discipline to maintain architectural consistency at scale.

#### TypeScript - Bun - Hono
The Bun runtime with Hono is largely comparable to Node.js with Express in terms of developer experience and architectural approach. Performance is modestly better in benchmarks, but Bun's compatibility with the Node.js ecosystem is not yet complete — some packages may cause issues. The long-term trajectory of the runtime depends on maintainer sustainability.

**Verdict:** A viable alternative to Node.js with incremental performance gains, offset by ecological and compatibility risk. Best suited for the same categories of projects as Express.

#### Python - FastAPI
Python's dynamic typing and idiomatic patterns tend to resist the explicit contract definitions that structured architectures favor. FastAPI's dependency injection and Pydantic validation provide useful scaffolding, but the language's inherent dynamism makes runtime contracts feel less stable than their static equivalents. Performance is a significant concern: even with async I/O, request throughput and latency are substantially lower than all other implementations tested, to a degree that would raise scalability concerns for moderately loaded services.

**Verdict:** Best suited for machine learning services or rapid prototyping. The performance gap and lack of static contracts make it a less compelling choice for general-purpose backend services.

### Architecture Impressions

#### Layered
The Layered (N-Tier) pattern is the most widely recognized and understood approach. It works well for small projects where distributing files across technical layers is simple to navigate. As projects grow, individual features become scattered across the codebase, making changes harder to locate and increasing coupling between layers. This pattern is a natural fit for Java and similar languages with loose import conventions and ORM-centric data models. It is a poor fit for Go, where package-level imports and strict visibility rules make cross-layer references cumbersome.

#### Vertical Slice
Vertical Slice (feature-based) architecture groups all code for a business capability into a single package, producing small, self-contained modules that are easy to navigate and modify. Slices should be loosely coupled with no circular dependencies. This pattern scales well because adding or changing a feature requires touching only one area of the codebase. It is applicable across all evaluated languages and can be combined with internal sub-layering as individual slices grow.

#### Hexagonal
Hexagonal Architecture enforces strict separation between domain logic and infrastructure through ports (interfaces) and adapters. This produces highly testable, framework-independent core logic but at the cost of significant boilerplate — mappers, DTOs, and separate entity types. The pattern's suitability varies strongly by language: it is a natural fit for Go, where implicit interfaces introduce almost no additional overhead. In Java, ORM entity requirements and the one-class-per-file convention inflate the number of artifacts. In Python, the absence of a native interface concept and the language's preference for direct imports over explicit contracts make hexagonal boundaries difficult to enforce.

#### Hive (Vertical Hexagonal)
Hive combines Vertical Slice locality with Hexagonal isolation. Each feature is a self-contained "cell" with its own domain, ports, and adapters, communicating with other cells only through defined interfaces. This preserves both navigability and architectural rigor. It is particularly well-suited to languages that already work well with Hexagonal patterns (Go, TypeScript), and it scales naturally toward a modular monolith or microservice extraction.

## Performance Summary

All benchmarks were run under comparable conditions (PostgreSQL backend, Docker deployment). Full per-endpoint breakdowns are available in each implementation's individual README.

> **Caveats:**
> - The Quarkus JVM implementation broke under heavy load (200 VUs / 3 min, 0.55% error rate) and did not recover afterward.
> - The Python FastAPI heavy-load data is incomplete due to a data cutoff during the test.
> - JVM-based implementations show significant warm-up: API test suite times drop from ~3 s on cold start to ~1 s after warm-up. Native (GraalVM) and Go runtimes show stable times.

### Resources & Throughput

| Implementation | Arch. | CPU | Memory | Max RPS | API Test Suite (cold → after load) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Spring Boot (Layered) | Layered | 6.44% | 580 MiB | 223 | 3.27 s → 1.01 s |
| Spring Boot (Vertical) | Vertical | 6.39% | 496 MiB | 197 | 3.30 s → 1.06 s |
| Spring Boot (Hexagonal) | Hexagonal | 7.04% | 502 MiB | 209 | 3.29 s → 1.13 s |
| Spring Boot (Hive) | Hive | 6.88% | 550 MiB | 215 | 3.04 s → 1.15 s |
| Quarkus (JVM) | Vertical | 12.70% | 624 MiB | 949 | 2.32 s → 1.05 s |
| Quarkus (GraalVM) | Vertical | 13.00% | 112 MiB | 782 | 1.21 s → 1.18 s |
| Micronaut (JVM) | Vertical | 12.70% | 344 MiB | 579 | 2.91 s → 1.75 s |
| Micronaut (GraalVM) | Vertical | 12.90% | 79 MiB | 521 | 1.47 s → 1.60 s |
| Go + Chi (Hexagonal) | Hexagonal | 12.90% | 24 MiB | 669 | 0.80 s → 0.80 s |
| Go + Chi (Hive) | Hive | 12.50% | 24 MiB | 751 | 0.80 s → 0.80 s |
| Node.js + Express (Hive) | Hive | 12.60% | 424 MiB | 254 | 1.72 s → 1.56 s |
| Bun + Hono (Hive) | Hive | 14.30% | 443 MiB | 289 | 1.54 s → 1.54 s |
| Python + FastAPI (Vertical) | Vertical | 4.17% | 67 MiB | 36 | 4.20 s → 4.39 s |

### Light Load (10 VUs / 30 s)

| Implementation | Avg RPS | Med Duration | p(90) Duration |
| :--- | ---: | ---: | ---: |
| Spring Boot (Layered) | 74 /s | 6.29 ms | 13.0 ms |
| Spring Boot (Vertical) | 74 /s | 5.90 ms | 12.5 ms |
| Spring Boot (Hexagonal) | 73 /s | 6.42 ms | 12.8 ms |
| Spring Boot (Hive) | 73 /s | 6.39 ms | 13.1 ms |
| Quarkus (JVM) | 75 /s | 3.47 ms | 6.5 ms |
| Quarkus (GraalVM) | 76 /s | 2.71 ms | 4.6 ms |
| Micronaut (JVM) | 76 /s | 3.67 ms | 11.3 ms |
| Micronaut (GraalVM) | 77 /s | 2.72 ms | 8.1 ms |
| Go + Chi (Hexagonal) | 78 /s | 2.38 ms | 4.6 ms |
| Go + Chi (Hive) | 77 /s | 2.50 ms | 4.9 ms |
| Node.js + Express (Hive) | 76 /s | 3.21 ms | 9.1 ms |
| Bun + Hono (Hive) | 75 /s | 2.50 ms | 5.6 ms |
| Python + FastAPI (Vertical) | 60 /s | 6.25 ms | 152.4 ms |

### Medium Load (50 VUs / 1 min)

| Implementation | Avg RPS | Med Duration | p(90) Duration |
| :--- | ---: | ---: | ---: |
| Spring Boot (Layered) | 222 /s | 4.92 ms | 21.6 ms |
| Spring Boot (Vertical) | 189 /s | 7.96 ms | 71.1 ms |
| Spring Boot (Hexagonal) | 197 /s | 6.33 ms | 31.1 ms |
| Spring Boot (Hive) | 210 /s | 6.39 ms | 30.0 ms |
| Quarkus (JVM) | 300 /s | 2.51 ms | 5.5 ms |
| Quarkus (GraalVM) | 296 /s | 2.82 ms | 6.4 ms |
| Micronaut (JVM) | 283 /s | 6.84 ms | 34.6 ms |
| Micronaut (GraalVM) | 286 /s | 7.09 ms | 30.3 ms |
| Go + Chi (Hexagonal) | 299 /s | 2.59 ms | 7.9 ms |
| Go + Chi (Hive) | 300 /s | 2.59 ms | 9.7 ms |
| Node.js + Express (Hive) | 182 /s | 37.89 ms | 379.7 ms |
| Bun + Hono (Hive) | 250 /s | 11.31 ms | 94.7 ms |
| Python + FastAPI (Vertical) | 32 /s | 914.74 ms | 2.74 s |

### Heavy Load (200 VUs / 3 min)

| Implementation | Avg RPS | Med Duration | p(90) Duration |
| :--- | ---: | ---: | ---: |
| Spring Boot (Layered) | 76 /s | 253.4 ms | 882.1 ms |
| Spring Boot (Vertical) | 71 /s | 305.4 ms | 1.04 s |
| Spring Boot (Hexagonal) | 85 /s | 328.7 ms | 933.8 ms |
| Spring Boot (Hive) | 80 /s | 324.6 ms | 1.05 s |
| Quarkus (JVM) ⚠️ | 499 /s | 13.1 ms | 104.5 ms |
| Quarkus (GraalVM) | 756 /s | 19.5 ms | 204.1 ms |
| Micronaut (JVM) | 562 /s | 107.5 ms | 328.6 ms |
| Micronaut (GraalVM) | 510 /s | 148.3 ms | 371.4 ms |
| Go + Chi (Hexagonal) | 648 /s | 78.2 ms | 229.7 ms |
| Go + Chi (Hive) | 734 /s | 54.6 ms | 172.5 ms |
| Node.js + Express (Hive) | 247 /s | 185.1 ms | 2.19 s |
| Bun + Hono (Hive) | 279 /s | 224.5 ms | 1.45 s |
| Python + FastAPI (Vertical) ⚠️ | 24 /s | 5.18 s | 18.87 s |

### Technology Ranking (Averaged Across Architectures)

Each technology is ranked per metric (1 = best) across all 14 performance dimensions (CPU, memory, max RPS, cold/warm API test, RPS/median/p90 at each load level), then averaged for an overall score.

| Rank | Technology         | Avg. Rank | Notable |
| :---: |:-------------------| ---: | :--- |
| 1 | Go + Chi           | 1.50 | Top 2 on nearly every metric; lowest memory and latency, highest heavy-load RPS |
| 2 | Java + Quarkus     | 2.57 | Highest max RPS and best heavy-load latency; higher CPU usage |
| 3 | Java + Micronaut   | 3.71 | Strong throughput, low memory; higher per-request latency |
| 4 | Bun + Hono         | 4.32 | Good light-load latency; highest CPU, degrades under load |
| 5 | Node.js + Express  | 4.75 | Adequate at low load; poor tail latency under load |
| 6 | Java + Spring Boot | 4.96 | Lowest CPU, good warm API test times; highest memory, weak heavy-load throughput |
| 7 | Python + FastAPI   | 6.25 | Low resource usage; far lowest throughput and highest latency |

> **Note:** The Quarkus JVM implementation exhibited instability under heavy load (see caveats above), which is not reflected in its aggregate numbers.

## Conclusion

The implementation that best balances developer experience, architectural clarity, and performance is **Go with Chi using a Hive architecture**. It produces code that is easy to understand at both the individual and structural level, scales naturally with additional features, contains no implicit behavior, and performs exceptionally well across all load profiles.

**Java with Micronaut using a Vertical Slice architecture** deserves mention as a strong alternative. It offers a developer experience similar to Spring Boot with significantly better throughput, making it well-suited for teams that prefer the JVM ecosystem.