# Load & Performance Tests

This directory contains scripts for benchmarking the performance and scalability of the various backend implementations using [k6](https://k6.io/).

## Purpose
The goal is to provide empirical data on how different tech stacks (e.g., Java vs. Go vs. Node.js) handle high traffic, focusing on:
- Latency (Response times)
- Throughput (Requests per second)
- Resource usage (CPU/RAM)

## Structure
The load tests are modularized and use a scenario-based approach:
- `src/load-test.ts`: The main entry point that defines k6 scenarios (Auth, Articles, Profiles, etc.) and orchestrates the test flow.
- `src/utils.ts`: Shared constants, helper functions, and the `setupUsers` logic for pre-registering a pool of test users.
- `src/groups/`: Directory containing modularized test logic grouped by API resource.

### Scenarios
The test runs multiple scenarios concurrently to simulate realistic load:
- **Auth**: Login and token validation.
- **Articles**: Article management (Create, List, Get, Favorite).
- **Profiles**: Profile interactions (Get Profile, Follow/Unfollow).
- **Comments**: Comment management on articles.
- **FullFlow**: A complete sequential user lifecycle.

### User Setup
To minimize overhead on the registration endpoint during performance testing, a `setup()` phase pre-registers 10 users. Virtual Users (VUs) then pick random users from this pool for their iterations.

**Note on Imports**: When adding new modules, always use the full `.ts` extension in imports (e.g., `import { ... } from './utils.ts'`) to ensure compatibility with k6's internal module loader when running inside Docker.

## Running Tests
Tests are configured to run via Docker to ensure a consistent environment.

### Local Test
Runs a quick smoke test against a backend running on `localhost:8080`.
```bash
npm run test:local
```

### Metrics & Monitoring
Runs the test and exports metrics to a Prometheus instance (configured for the `realworld-network`).
```bash
npm run test:metrics
```

### Load Presets
Both `test:local` and `test:metrics` can be executed with predefined load intensities:
- **Light** (10 VUs, 30s): `npm run test:local:light` / `npm run test:metrics:light`
- **Medium** (50 VUs, 1m): `npm run test:local:medium` / `npm run test:metrics:medium`
- **Heavy** (200 VUs, 3m): `npm run test:local:heavy` / `npm run test:metrics:heavy`

### Custom Values
You can define custom Virtual Users (`vus`) and Durations on the fly by passing arguments through NPM:
```bash
npm run test:local -- --vus 75 --duration 2m
```

## Monitoring
Visualize results in real-time using the Prometheus/Grafana stack defined in the `deployment/` directory.
