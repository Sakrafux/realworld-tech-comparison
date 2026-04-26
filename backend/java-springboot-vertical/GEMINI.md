# Gemini AI Instructions for RealWorld Backend

This file contains specific rules, architectural mandates, and coding conventions for this Java Spring Boot implementation of the RealWorld API. As an AI assistant, you **MUST** prioritize these rules over your general default behaviors when operating within this project.

## 1. Architectural Mandates

*   **Vertical Slice / Package-by-Feature Architecture:** Code MUST be organized by business domain/feature (e.g., `article`, `user`, `comment`, `tag`, `profile`) rather than by technical layer.
    *   **Feature Packages:** A single package MUST contain all the components required for that specific feature to work (Controller, Service, Repository, Entity, DTOs, and Mappers).
    *   **Encapsulation:** Keep classes public, but don't use them outside their package. Typically, only the REST Controller or a specific public interface should be exposed outside the package.
    *   **Cross-Cutting Concerns:** Code that spans multiple features or represents infrastructure (Security, Global Exceptions, Configurations, Base Entities) MUST reside in a `core` or `security` package.
*   **Entities:** Extend `BaseEntity` (located in the `core` package) to inherit auditing fields (`createdAt`, `updatedAt`).

## 2. Testing Philosophy

*   **Naming Convention:** Test methods MUST follow the `action_condition_expectedResult` pattern.
    *   *Example:* `registerUser_ValidUser_SavesAndReturnsUser`
    *   *Example:* `getTags_TagsExist_ReturnsOkWithTags`
*   **Integration Tests:** API endpoints must be tested using `MockMvc` in classes suffixed with `IT` (e.g., `UserControllerIT`). Verify HTTP status codes and JSON payloads. These should be co-located in the same feature package structure under `src/test/java`.
*   **Unit Tests:** Business logic must be thoroughly unit-tested using Mockito, in classes suffixed with `Test` (e.g., `UserServiceTest`). Mappers MUST NOT be mocked; use the real implementation (via `Mappers.getMapper`) or a `@Spy` to ensure mapping logic is actually exercised.

## 3. Exception Handling & Validation

*   **Global Exception Handler:** NEVER handle standard business exceptions inside controllers. Throw custom exceptions and let `GlobalExceptionHandler` (located in `core.exception`) translate them into appropriate HTTP responses.
*   **Custom Exceptions:** Use domain-specific exceptions. General ones should be in the `core.exception` package. Feature-specific exceptions should reside in their respective feature package.
    *   `ResourceNotFoundException` -> 404 Not Found
    *   `ResourceAlreadyExistsException` -> 422 Unprocessable Entity
    *   `InvalidCredentialsException` -> 401 Unauthorized
*   **Validation:** Use `jakarta.validation` annotations (`@NotBlank`, `@Email`, `@Size`, etc.) on Request DTOs. `GlobalExceptionHandler` automatically maps `MethodArgumentNotValidException` to a 422 response with structured error messages.

## 4. Security & JWT

*   **Stateless:** The application uses stateless JWT authentication. Do not rely on HTTP sessions.
*   **Context:** Check if `SecurityContextHolder.getContext().getAuthentication() == null` before authenticating users in custom filters to avoid overriding existing authentications.
*   **Passwords:** Always encode passwords using `PasswordEncoder` before saving to the database.

## 5. Code Style & Libraries

*   **Lombok:** Use Lombok to reduce boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`).
*   **MapStruct:** Use MapStruct for mapping between Entities and DTOs.
*   **Constants:** Use constant notation (`UPPER_SNAKE_CASE`) only for `final` value variables (primitives, Strings, or value-like objects) that are **initialized directly** at the site of declaration. If a final variable is initialized via a constructor (explicitly or via Lombok), it MUST use standard `camelCase` naming. This applies to both private and public fields.
*   **Documentation:** Always provide descriptive class-level and method-level JavaDocs for Controllers, Services, Exceptions, and Configuration classes. Ensure you include `@param`, `@return`, and `@throws` tags where applicable.

## 6. Workflow Mandates

*   **Surgical Updates:** When modifying existing files, ONLY change the affected areas using the `replace` tool. DO NOT overwrite entire files with `write_file` unless creating a new file from scratch. This preserves context and minimizes unnecessary changes.
