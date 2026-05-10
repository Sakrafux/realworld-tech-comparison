# Gemini AI Instructions for Go Chi RealWorld Backend (Hive)

This file contains specific rules, architectural mandates, and coding conventions for this Go implementation of the RealWorld API using **Vertical Hexagonal Architecture (Hive)** and the `chi` router. As an AI assistant, you **MUST** prioritize these rules over your general default behaviors when operating within this project.

## 1. Architectural Mandates: The Hive

This implementation uses a vertical slice approach where business features are encapsulated in "cells".

*   **Shared Layer (`internal/shared`):** Contains technical primitives (config, database, security, web, errors). It is business-agnostic and depends on nothing but external libraries.
*   **Cell Layer (`internal/cells`):** Each subdirectory is a self-contained vertical slice (mini-hexagon).
    *   **Encapsulation:** A cell contains its own `domain.go`, `ports.go`, `service.go`, `repository.go`, and `handler.go`.
    *   **Independence:** Cells SHOULD NOT import other cells' repositories or handlers.
    *   **Communication:** Cells communicate via interfaces defined in their own `ports.go` (e.g., `UserProvider`). These interfaces are satisfied by other cells' services.
*   **The Hive (`internal/hive`):** The Composition Root (`app.go`). It is the ONLY place where cells are instantiated and wired together.
*   **Dependency Rule:** `shared` -> `cells` -> `hive` -> `cmd/main`.

## 2. Testing Philosophy

*   **Unit Tests:** Reside in the same package as the code they test (e.g., `service_test.go` in `internal/cells/article`).
    *   Use `testify/assert` for assertions.
    *   Use `testify/mock` for mocking dependencies.
*   **Integration Tests:** Reside in `tests/integration`. They test the full HTTP stack using the actual `hive.App` and an in-memory SQLite database. Files are suffixed with `_api_test.go`.

## 3. Exception Handling & Validation

*   **AppError:** Use the custom `errors.AppError` defined in `internal/shared/errors`. Return them from services and handle them in handlers.
*   **Error Mapping:** `web.RespondWithError` in `internal/shared/web/error_handler.go` maps `AppError` types to appropriate HTTP status codes.
*   **Validation:** Use `github.com/go-playground/validator/v10` on request structs in handlers. Business-level validation should be in the domain or service within the cell.

## 4. Security & JWT

*   **Stateless:** The application uses stateless JWT authentication.
*   **Context:** Authentication middleware extracts the user ID and adds it to the request context. Use `web.GetUserIDFromContext` to retrieve it.
*   **Encryption:** Use `bcrypt` for password hashing via `internal/shared/security`.

## 5. Code Style & Libraries

*   **Router:** Use `github.com/go-chi/chi/v5`.
*   **Database:** Use `github.com/jmoiron/sqlx` for SQL operations and `modernc.org/sqlite` for the database driver.
*   **Logging:** Use `github.com/go-chi/httplog/v2` for structured logging.
*   **Naming:** Follow standard Go conventions (`CamelCase` for exported, `camelCase` for unexported).

## 6. Workflow Mandates

*   **Surgical Updates:** When modifying existing files, ONLY change the affected areas using the `replace` tool. DO NOT overwrite entire files with `write_file` unless creating a new file from scratch.
*   **Vertical Cohesion:** When adding a feature, keep all logic within the relevant cell directory. Only touch `internal/hive` to wire new dependencies.
