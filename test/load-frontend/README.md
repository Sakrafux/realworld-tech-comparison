# Load & Performance Tests

This directory contains scripts for benchmarking the performance and scalability of the various frontend implementations using [k6](https://k6.io/).

## Purpose
The goal is to provide empirical data on how different tech stacks handle high traffic, focusing on:
- Latency (Response times)
- Throughput (Requests per second)

## Structure
The simple test suite is located entirely in `src/test.ts`.

### Scenarios
The test runs multiple scenarios concurrently to simulate realistic load:
- **Home**: Loading the home page.
- **Profile**: Loading a profile.
- **Article**: Loading an article

## Running Tests
Tests must be run using a locally installed `k6`.

### Local Test
Runs a quick smoke test against a frontend running on `localhost:3000`.
```bash
k6 run src/test.ts
```