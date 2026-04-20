# API Tests

This directory contains a scripting-based functional test suite to verify that a deployed backend implementation is alive and strictly adhering to the API contract.

## Purpose
These tests focus strictly on the **Backend API** and go beyond simple "Happy Path" checks. They are designed to:
- **Contract Validation**: Ensure all endpoints return the correct data structures and HTTP status codes.
- **Negative Testing & Edge Cases**: Verify that the API handles invalid input, unauthorized access, missing tokens, and various edge cases correctly (e.g., non-existent entities, duplicate entries, invalid payloads).
- **Workflow Verification**: Execute multi-step sequences (e.g., Register → Create Article → Follow Author → Verify feed → Delete Article).
- **Exhaustive Coverage**: Includes full coverage of `Articles`, `Comments`, `Profiles`, `Tags`, `User` and `Users` endpoints as documented in `api/openapi.yml`.

## Tech Stack
- **TypeScript**: For strong typing and better developer experience.
- **Vitest**: As the test runner for its rich assertion libraries, speed, and great watch mode.
- **Native Node Fetch**: For lightweight, built-in HTTP requests, wrapped in a generic typed client (`apiClient.ts`).

## Setup and Running
1.  **Install dependencies**:
    ```bash
    npm install
    ```
2.  **Configure environment**:
    Ensure `.env` contains the correct `API_BASE_URL`. If omitted, it defaults to `http://localhost:8080/api`.
3.  **Run tests**:
    ```bash
    npm test          # Watch mode
    npm run test:run  # Single run (CI)
    ```

## Structure
- `tests/**/*.spec.ts`: Test files categorized by API scopes (articles, comments, profiles, tags, user, users).
- `utils/apiClient.ts`: Typed wrapper around native `fetch`.
- `utils/testUtils.ts`: Helpers for generating unique test data and automating prerequisites (e.g., registration).