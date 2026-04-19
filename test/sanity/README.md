# Sanity Tests

This directory contains a scripting-based functional test suite to verify that a deployed backend implementation is alive and strictly adhering to the API contract.

## Purpose
These tests focus strictly on the **Backend API** and go beyond simple "Happy Path" checks. They are designed to:
- **Contract Validation**: Ensure all endpoints return the correct data structures and HTTP status codes.
- **Negative Testing**: Verify that the API handles invalid input, unauthorized access, and edge cases correctly.
- **Workflow Verification**: Execute multi-step sequences (e.g., Register -> Create Article -> Delete Article -> Verify deletion).

## Tech Stack
- **JavaScript (Node.js)**: The primary choice for scripting.
- **Vitest** or **Jest**: As the test runner for its rich assertion libraries and speed.
- **Axios** or **Supertest**: For making clean, promise-based HTTP requests.
