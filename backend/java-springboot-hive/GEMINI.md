# Gemini AI Instructions for RealWorld Backend (Hive - Vertical Hexagonal)

This file contains specific rules, architectural mandates, and coding conventions for this Java Spring Boot implementation of the RealWorld API using the "Hive" architecture (Vertical Hexagonal). As an AI assistant, you **MUST** prioritize these rules over your general default behaviors when operating within this project.

## 1. Architectural Mandates

*   **Hive Architecture (Vertical Hexagonal):** Code MUST be organized by business feature (e.g., `article`, `user`), where each feature is a self-contained "cell" containing its own Hexagonal layers.
    *   **Feature-Isolated Cells:** Each package (e.g., `com.sakrafux.realworld.article`) is responsible for its own business logic, API endpoints, and persistence.
    *   **Internal Hexagon Structure:** Within each feature package:
        *   `domain/`: Core business entities (POJOs) and domain services. NO dependencies on frameworks.
        *   `application/`: Use cases and Ports.
            *   `port/in`: Input Ports (Use Case interfaces).
            *   `port/out`: Output Ports (Repository/Service interfaces).
            *   `service/`: Use Case implementations orchestrating domain objects.
        *   `infrastructure/`: Adapters.
            *   `web/`: REST Controllers and Web DTOs.
            *   `persistence/`: JPA Entities and Repository implementations.
    *   **Port-Based Communication:** Cells MUST NOT touch each other's database entities or repositories directly. Cross-cell interaction MUST happen only through the **Input Ports** of the target cell.
    *   **Encapsulation:** Prefer package-private visibility for infrastructure details (JPA Entities, DTOs, Repository implementations) within the feature package to enforce boundaries.
    *   **Shared Core:** Cross-cutting concerns (Security, Global Exceptions, Configurations) reside in a `core` package.

## 2. Testing Philosophy

*   **Naming Convention:** Test methods MUST follow the `action_condition_expectedResult` pattern.
    *   *Example:* `registerUser_ValidUser_SavesAndReturnsUser`
*   **Integration Tests:** API endpoints must be tested using `MockMvc` in classes suffixed with `IT` (e.g., `UserControllerIT`). These should be co-located within the feature's infrastructure package or a corresponding test package.
*   **Unit Tests:** Thoroughly unit-test Use Case implementations and Domain logic using Mockito to mock Output Ports. Classes suffixed with `Test`.
*   **Mappers:** MUST NOT be mocked; use the real implementation or a `@Spy`.

## 3. Exception Handling & Validation

*   **Global Exception Handler:** Centralized in `core.exception`. NEVER handle standard business exceptions inside controllers.
*   **Custom Exceptions:** Use domain-specific exceptions. General ones in `core.exception`, feature-specific ones in their respective feature package.
*   **Validation:** Use `jakarta.validation` annotations on Request DTOs in the `infrastructure/web` layer.

## 4. Security & JWT

*   **Stateless:** The application uses stateless JWT authentication (located in `core.security`).
*   **Context:** Security is an infrastructure/core concern. Use Case implementations should be agnostic of the specific security mechanism.
*   **Passwords:** Always encode passwords using `PasswordEncoder` before saving.

## 5. Code Style & Libraries

*   **Lombok:** Use `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`.
*   **MapStruct:** Use for mapping between Infrastructure DTOs, Application models, and Domain Entities.
*   **Constants:** Use `UPPER_SNAKE_CASE` only for `final` variables initialized directly at declaration. Use `camelCase` for `final` variables initialized via constructor.
*   **Documentation:** Provide JavaDocs for Ports, Use Case implementations, and Adapters.

## 6. Workflow Mandates

*   **Surgical Updates:** When modifying existing files, ONLY change the affected areas using the `replace` tool. DO NOT overwrite entire files with `write_file` unless creating a new file from scratch.
