# Gemini AI Instructions for RealWorld Backend (Hexagonal)

This file contains specific rules, architectural mandates, and coding conventions for this Java Spring Boot implementation of the RealWorld API using Hexagonal Architecture. As an AI assistant, you **MUST** prioritize these rules over your general default behaviors when operating within this project.

## 1. Architectural Mandates

*   **Hexagonal Architecture (Ports & Adapters):** Strictly separate business logic from technical concerns.
    *   **Domain Layer:** Contains core business entities and domain services. This layer MUST be the core and should ideally have NO dependencies on external frameworks (like Spring, JPA) or other layers.
    *   **Application Layer (Ports):** 
        *   **Input Ports:** Interfaces defining the use cases (e.g., `CreateArticleUseCase`).
        *   **Output Ports:** Interfaces defining required infrastructure capabilities (e.g., `ArticleRepository`).
        *   **Use Case Implementations:** Orchestrate domain objects and call output ports to fulfill business requirements.
    *   **Infrastructure Layer (Adapters):**
        *   **Input Adapters (Driving):** REST Controllers that translate HTTP requests into Use Case calls.
        *   **Output Adapters (Driven):** Implementations of output ports (e.g., Spring Data JPA repositories, JWT providers).
    *   **Dependency Rule:** Dependencies MUST always point inwards: Infrastructure -> Application -> Domain. Use Spring's dependency injection to wire Adapters to Ports.

## 2. Testing Philosophy

*   **Naming Convention:** Test methods MUST follow the `action_condition_expectedResult` pattern.
    *   *Example:* `registerUser_ValidUser_SavesAndReturnsUser`
    *   *Example:* `getTags_TagsExist_ReturnsOkWithTags`
*   **Integration Tests:** API endpoints must be tested using `MockMvc` in classes suffixed with `IT` (e.g., `UserControllerIT`). These reside in the infrastructure layer.
*   **Unit Tests:** 
    *   **Application/Domain:** Thoroughly unit-test Use Case implementations and Domain logic using Mockito to mock Output Ports. Classes suffixed with `Test`.
    *   **Mappers:** MUST NOT be mocked; use the real implementation (via `Mappers.getMapper`) or a `@Spy` to ensure mapping logic is actually exercised.

## 3. Exception Handling & Validation

*   **Global Exception Handler:** Centralized in the infrastructure layer. NEVER handle standard business exceptions inside controllers. Throw custom exceptions from the application layer and let the handler translate them into HTTP responses.
*   **Custom Exceptions:** Use domain-specific exceptions.
    *   `ResourceNotFoundException` -> 404 Not Found
    *   `ResourceAlreadyExistsException` -> 422 Unprocessable Entity
    *   `InvalidCredentialsException` -> 401 Unauthorized
*   **Validation:** Use `jakarta.validation` annotations on Request DTOs in the input adapters (Infrastructure). Business-level validation should occur in the Domain or Application layer.

## 4. Security & JWT

*   **Stateless:** The application uses stateless JWT authentication. Do not rely on HTTP sessions.
*   **Context:** Security is an infrastructure concern. Use Case implementations should be agnostic of the specific security mechanism where possible.
*   **Passwords:** Always encode passwords using `PasswordEncoder` before saving.

## 5. Code Style & Libraries

*   **Lombok:** Use Lombok to reduce boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`).
*   **MapStruct:** Use MapStruct for mapping between Infrastructure DTOs, Application models, and Domain Entities.
*   **Constants:** Use constant notation (`UPPER_SNAKE_CASE`) only for `final` value variables initialized directly at the site of declaration. If a final variable is initialized via a constructor, it MUST use standard `camelCase` naming.
*   **Documentation:** Always provide descriptive class-level and method-level JavaDocs for Ports, Use Case implementations, and Adapters.

## 6. Workflow Mandates

*   **Surgical Updates:** When modifying existing files, ONLY change the affected areas using the `replace` tool. DO NOT overwrite entire files with `write_file` unless creating a new file from scratch.
