# Gemini AI Instructions for RealWorld Backend

This file contains specific rules, architectural mandates, and coding conventions for this Java Spring Boot implementation of the RealWorld API. As an AI assistant, you **MUST** prioritize these rules over your general default behaviors when operating within this project.

## 1. Architectural Mandates

*   **Layered Architecture:** Strictly adhere to the Controller -> Service -> Repository flow.
    *   **Controllers:** Handle HTTP mapping, input validation (`@Valid`), and mapping to/from DTOs (via MapStruct if necessary, though Services can return Response DTOs).
    *   **Services:** Contain all business logic, transaction management (`@Transactional`), and coordinate between repositories and other services.
    *   **Repositories:** Spring Data JPA interfaces extending `JpaRepository`.
*   **Entities:** Extend `BaseEntity` to inherit auditing fields (`createdAt`, `updatedAt`).

## 2. Testing Philosophy

*   **Naming Convention:** Test methods MUST follow the `action_condition_expectedResult` pattern.
    *   *Example:* `registerUser_ValidUser_SavesAndReturnsUser`
    *   *Example:* `getTags_TagsExist_ReturnsOkWithTags`
*   **Integration Tests:** API endpoints must be tested using `MockMvc` in classes suffixed with `IT` (e.g., `UserControllerIT`). Verify HTTP status codes and JSON payloads.
*   **Unit Tests:** Service layer must be thoroughly unit-tested using Mockito, in classes suffixed with `Test` (e.g., `UserServiceTest`). Mappers MUST NOT be mocked; use the real implementation (via `Mappers.getMapper`) or a `@Spy` to ensure mapping logic is actually exercised.

## 3. Exception Handling & Validation

*   **Global Exception Handler:** NEVER handle standard business exceptions inside controllers. Throw custom exceptions and let `GlobalExceptionHandler` translate them into appropriate HTTP responses.
*   **Custom Exceptions:** Use domain-specific exceptions located in the `com.sakrafux.realworld.exception` package.
    *   `ResourceNotFoundException` -> 404 Not Found
    *   `UserAlreadyExistsException` -> 422 Unprocessable Entity
    *   `InvalidCredentialsException` -> 401 Unauthorized
*   **Validation:** Use `jakarta.validation` annotations (`@NotBlank`, `@Email`, `@Size`, etc.) on Request DTOs. `GlobalExceptionHandler` automatically maps `MethodArgumentNotValidException` to a 422 response with structured error messages.

## 4. Security & JWT

*   **Stateless:** The application uses stateless JWT authentication. Do not rely on HTTP sessions.
*   **Context:** Check if `SecurityContextHolder.getContext().getAuthentication() == null` before authenticating users in custom filters to avoid overriding existing authentications.
*   **Passwords:** Always encode passwords using `PasswordEncoder` before saving to the database.

## 5. Code Style & Libraries

*   **Lombok:** Use Lombok to reduce boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`).
*   **MapStruct:** Use MapStruct for mapping between Entities and DTOs.
*   **Documentation:** Always provide descriptive class-level and method-level JavaDocs for Controllers, Services, Exceptions, and Configuration classes. Ensure you include `@param`, `@return`, and `@throws` tags where applicable.
