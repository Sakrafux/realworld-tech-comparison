# RealWorld Tech Comparison

This repository is dedicated to comparing various tech stacks and architectures using the [RealWorld](https://docs.realworld.show/introduction/) (Conduit) application as a benchmark.

## Project Structure

- `backend/`: Different backend implementations (e.g., Java, Node.js, Go).
- `frontend/`: Various frontend frameworks and state management libraries (e.g., React, Svelte).
- `db/`: Database configurations, migrations, and schema definitions.
- `test/`: Comprehensive testing suite, including load testing and end-to-end tests.

## Backend Performance Ranking

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

## Architecture Discussion

Understanding software architecture requires distinguishing between different levels of abstraction. This project focuses primarily on **Code Architecture**, but it sits within a broader hierarchy of architectural decisions.

### 1. Enterprise Architecture
At the highest level, Enterprise Architecture aligns IT strategy with business goals. It is highly individual to a business's specific application landscape, organizational structure, and operational processes. It deals with portfolios of applications, master data management, business capabilities, and how different domains within an enterprise interact (e.g., CRM, ERP, and bespoke applications). 

### 2. System Architecture (Macro-Architecture)
System Architecture defines how a specific system or application is deployed and how its major components communicate over a network. The primary choices here involve scalability, deployability, and operational boundaries:
- **Monolith**: A single, cohesive unit of deployment. Easy to develop and deploy initially but can become difficult to scale and maintain as it grows.
- **Modular Monolith**: A monolith where internal components are strictly separated by bounded contexts (often using code architecture patterns like Hive or Hexagonal), combining the deployment simplicity of a monolith with the organizational benefits of microservices.
- **Service-Oriented Architecture (SOA)**: An architectural style that supports service orientation. While historically associated with heavy Enterprise Service Buses (ESBs), modern SOA often achieves integration using simpler API gateways and lighter protocols.
- **Microservices**: Highly decoupled, independently deployable services organized around business capabilities. Excellent for organizational scaling and independent technology choices, but introduces distributed system complexity (networking, data consistency, observability).

### 3. Code Architecture (Micro-Architecture)
Code Architecture dictates how the source code within a single deployable unit (a monolith or a single microservice) is organized. This is the primary focus of the different backend implementations in this repository:
- **Layered (N-Tier)**: Organizes code by technical concerns (Controller, Service, Repository). Simple and ubiquitous, but often leads to high coupling and scattered business logic.
- **Vertical Slice**: Organizes code strictly by business feature or use case. High cohesion, as everything needed for a feature lives in one place.
- **Hexagonal (Ports and Adapters)**: Isolates the core domain logic from technical infrastructure (DB, Web) using interfaces (ports) and implementations (adapters). Highly testable and tech-agnostic.
- **Hive (Vertical Hexagonal)**: A hybrid approach that combines the feature grouping of Vertical Slices with the strict boundary enforcement of Hexagonal Architecture.

### Adjacent Architectural Concepts
These are augmenting patterns that can be integrated at both the System and Code architecture levels:
- **Event-Driven Architecture (EDA)**: Components communicate by producing and consuming events. At the system level, this involves message brokers (Kafka, RabbitMQ) to decouple microservices; at the code level, it involves domain events within the application process to decouple internal modules.
- **CQRS (Command Query Responsibility Segregation)**: Separates read operations (Queries) from write operations (Commands). Often used alongside Event Sourcing, it allows scaling read and write models independently and simplifies complex data retrieval.
- **Event Sourcing**: Instead of storing the current state of a domain entity, this pattern stores a sequence of immutable, state-changing events (the "source of truth"). The current state is derived by replaying these events. It provides a built-in audit trail, enables "time travel" debugging, and is typically paired with CQRS to project events into optimized read databases.
