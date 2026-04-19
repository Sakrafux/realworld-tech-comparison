# RealWorld Backend: Java Spring Boot (Layered Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java and Spring Boot.

## Architecture

This implementation follows a classic **Layered Architecture** (also known as N-Tier Architecture):

- **Web/Controller Layer**: Handles incoming HTTP requests and maps them to service calls.
- **Service Layer**: Contains the business logic and orchestrates data flow.
- **Persistence/Repository Layer**: Manages data access and interaction with the database.
- **Domain Layer**: Defines the core entities and data models.

## Tech Stack

- **Java**
- **Spring Boot**
- **Spring Data JPA**
- **H2/PostgreSQL** (depending on configuration)
- **Maven**
