# API Tests

This directory contains a scripting-based functional test suite to verify that a deployed backend implementation is alive and strictly adhering to the API contract.

## Purpose
These tests focus strictly on the **Backend API** and go beyond simple "Happy Path" checks. They are designed to:
- **Contract Validation**: Ensure all endpoints return the correct data structures and HTTP status codes.
- **Negative Testing**: Verify that the API handles invalid input, unauthorized access, and edge cases correctly.
- **Workflow Verification**: Execute multi-step sequences (e.g., Register -> Create Article -> Delete Article -> Verify deletion).

## Tech Stack
- **TypeScript**: For strong typing and better developer experience.
- **Vitest**: As the test runner for its rich assertion libraries and speed.
- **Native Node Fetch**: For lightweight, built-in HTTP requests.

## Setup and Running
1.  **Install dependencies**:
    ```bash
    npm install
    ```
2.  **Configure environment**:
    Ensure `.env` contains the correct `API_BASE_URL`.
3.  **Run tests**:
    ```bash
    npm test          # Watch mode
    npm run test:run  # Single run (CI)
    ```

## Structure
- `tests/**/*.spec.ts`: Test files.
- `utils/apiClient.ts`: Typed wrapper around `fetch`.
