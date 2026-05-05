# Gemini AI Instructions for Go Chi RealWorld Backend (Hexagonal)

This file contains specific rules, architectural mandates, and coding conventions for this Go implementation of the RealWorld API using Hexagonal Architecture and the `chi` router. As an AI assistant, you **MUST** prioritize these rules over your general default behaviors when operating within this project.

## 1. Architectural Mandates

*   **Hexagonal Architecture (Ports & Adapters):** Strictly separate business logic from technical concerns.
    *   **Domain Layer (`internal/domain`):** Contains core business entities and logic (e.g., `Article`, `User`). It MUST NOT depend on any other layers or external infrastructure libraries (except basic Go standard library).
    *   **Application Layer (`internal/application`):**
        *   **Ports (`internal/application/port`):** Interfaces defining the input (services) and output (repositories) requirements.
        *   **Services (`internal/application/service`):** Implement the service ports. They orchestrate domain objects and call output ports to fulfill business requirements.
    *   **Infrastructure Layer (`internal/infrastructure`):**
        *   **Persistence (`internal/infrastructure/persistence`):** Implement repository ports using `sqlx`.
        *   **Web (`internal/infrastructure/web`):** Handlers (input adapters) that translate HTTP requests into service calls and manage HTTP responses.
        *   **Security (`internal/infrastructure/security`):** JWT and password hashing implementations.
        *   **Configuration (`internal/infrastructure/configuration`):** App initialization and database setup.
    *   **Dependency Rule:** Dependencies MUST always point inwards: Infrastructure -> Application -> Domain. Use constructor injection to wire components.

## 2. Testing Philosophy

*   **Unit Tests:** Reside in the same package as the code they test (e.g., `article_service_test.go` in `internal/application/service`).
    *   Use `testify/assert` for assertions.
    *   Use `testify/mock` and the `testmocks` package for mocking dependencies.
*   **Integration Tests:** Reside in `tests/integration`. They test the full HTTP stack using the actual router and an in-memory SQLite database. Files are suffixed with `_api_test.go`.
*   **Mocking:** All repository and service interfaces have manually maintained mocks in `tests/testmocks/mocks.go`. You MUST update these mocks if the interface changes.

## 3. Exception Handling & Validation

*   **AppError:** Use the custom `domain.AppError` for business-level errors. Return them from services and handle them in the web layer.
*   **Error Mapping:** `RespondWithError` in `internal/infrastructure/web/error_handler.go` maps `AppError` types to appropriate HTTP status codes (404, 401, 403, 422, etc.).
*   **Validation:** Use `github.com/go-playground/validator/v10` on request structs in the web layer. Business-level validation should be in the domain or service layer.

## 4. Security & JWT

*   **Stateless:** The application uses stateless JWT authentication.
*   **Context:** Authentication middleware extracts the user ID and adds it to the request context. Use `GetUserIDFromContext` to retrieve it.
*   **Encryption:** Use `bcrypt` for password hashing via the security infrastructure.

## 5. Code Style & Libraries

*   **Router:** Use `github.com/go-chi/chi/v5`.
*   **Database:** Use `github.com/jmoiron/sqlx` for SQL operations and `modernc.org/sqlite` for the database driver.
*   **Logging:** Use `github.com/go-chi/httplog/v2` for structured logging.
*   **Naming:** Follow standard Go conventions (`CamelCase` for exported, `camelCase` for unexported). Tests should follow the `Test[Component]_[Action]` naming pattern.

## 6. Workflow Mandates

*   **Surgical Updates:** When modifying existing files, ONLY change the affected areas using the `replace` tool. DO NOT overwrite entire files with `write_file` unless creating a new file from scratch.
*   **Mock Updates:** If you add methods to ports, manually update `tests/testmocks/mocks.go`.
