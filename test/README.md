# Testing Suite

This directory contains cross-cutting tests to ensure all implementations of the RealWorld Tech Comparison project meet the required standards. The suite is divided into three main testing strategies.

## 1. API Contract Validation (`api/`)

An independent, network-based functional test suite to verify that a deployed backend Strictly adheres to the RealWorld API contract.

- **Scope**: Goes beyond simple "Happy Path" checks to cover negative testing, edge cases (invalid payloads, missing tokens), and complex workflows.
- **Tech Stack**: TypeScript, Vitest, and native Node `fetch` (via a typed wrapper).
- **Usage**:
  - `npm test` - Run tests in watch mode.
  - `npm run test:run` - Single run (ideal for CI pipelines).
  - *Note: Target URL is configured via `.env` (`API_BASE_URL`), defaulting to `http://localhost:8080/api`.*

## 2. Load & Performance Testing (`load/`)

Modular scripts for benchmarking performance and scalability across different backend implementations, providing empirical data on latency, throughput, and resource usage.

- **Tech Stack**: Written in TypeScript and executed via [k6](https://k6.io/) running consistently inside Docker (`docker-compose`).
- **Monitoring**: Natively integrates with the Prometheus/Grafana stack (defined in the `deployment/` directory) to export metrics for real-time visualization.
- **Usage**:
  - `npm run test:local` - Runs a quick smoke test against `localhost:8080`.
  - `npm run test:metrics` - Runs the load test and exports metrics to the `realworld-network` Prometheus instance.
- **Tuning & Presets**: 
  - Use suffixes for predefined loads: `:light` (10 VUs / 30s), `:medium` (50 VUs / 1m), or `:heavy` (200 VUs / 3m). Example: `npm run test:metrics:medium`.
  - Pass custom parameters dynamically using NPM's argument pass-through: `npm run test:local -- --vus 75 --duration 2m`.

## 3. End-to-End Testing (`e2e/`)

End-to-end tests designed to verify full-stack functionality from the frontend to the backend.
