# RealWorld Backend: Java Quarkus (Vertical Slice Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java and Quarkus.

## Architecture

This implementation follows a **Vertical Slice Architecture** (Package-by-Feature) combined with the **Active Record Pattern**:

- **Feature-Centric**: Each package (e.g., `article`, `user`, `comment`) contains all the components required for that specific feature, including REST Resources, Services, Mappers, and Entities.
- **Active Record (Panache)**: By leveraging **Hibernate Panache**, business entities extend `PanacheEntityBase`. This combines data and persistence logic, significantly reducing boilerplate code while maintaining high performance.
- **High Cohesion**: Business logic, data access, and API definitions for a single domain are kept together, making features easier to find and modify in isolation.
- **Internal Simplicity**: Because the slices are granular, there is no internal sub-layering. Resources, services, and entities live side-by-side for maximum visibility and developer productivity.
- **Cross-Cutting Concerns**: Shared infrastructure (Security, Global Exceptions, Configurations, Base Entities) resides in a specialized `core` package.

## Tech Stack

- **Java 25**
- **Quarkus 3.35.2**
- **Hibernate Panache** (Active Record Pattern)
- **H2 / PostgreSQL** (depending on configuration)
- **Maven**
- **Monitoring**: Micrometer Registry OTLP, OpenTelemetry, Prometheus & Grafana integration
- **Security**: Stateless JWT Authentication via SmallRye JWT

## Directory Structure (Current: Granular Slices)

In this approach, slices are kept as small as possible to minimize complexity within a single package.

```text
src/
└── main/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── article/      # Article feature (Entity, Resource, Service, Mapper)
    │       ├── comment/      # Comment feature
    │       ├── user/         # User & Profile feature
    │       └── core/         # Shared infrastructure (Security, Exceptions)
    └── resources/            # application.yml
```

## Testing

This project prioritizes the global **API Test Suite** (located in the root `/test/api` directory) as the primary verification tool for specification compliance. 

The module contains a comprehensive suite of **Integration Tests** (`*IT.java`) using `@QuarkusTest`. These tests verify the interaction between Resources, Services, and Panache Entities. Native build verification is supported via `@QuarkusIntegrationTest`.

## Building

In order for the docker containers to be built, the jar (or native image) must first be built. Because Quarkus 
locks the DB at build time, this cannot be overwritten using environment variables. The same goes for OpenTelemetry.

Overall, one must be careful with Quarkus and build/run time settings.

### JVM

```shell
./mvnw clean package "-Dquarkus.datasource.db-kind=postgresql" "-Dquarkus.otel.enabled=true" -DskipTests
```

### GraalVM

```shell
...
```

## JVM Performance

- Max CPU Utilization: -
- Max Memory Usage: -
- Max Requests per Second: -

<!-- Performance chart placeholder -->

### API test suite

- On startup: -
- After 10 warm-up runs: -
- After load test: -

### Load test suite

#### Light load (10 VUs / 30s)

    TBD

#### Medium load (50 VUs / 1m)

    TBD

#### Heavy load (200 VUs / 3m)

    TBD

## GraalVM Performance

- Max CPU Utilization: -
- Max Memory Usage: -
- Max Requests per Second: -

<!-- Performance chart placeholder -->

### API test suite

- On startup: -
- After 10 warm-up runs: -
- After load test: -

### Load test suite

#### Light load (10 VUs / 30s)

    TBD

#### Medium load (50 VUs / 1m)

    TBD

#### Heavy load (200 VUs / 3m)

    TBD
