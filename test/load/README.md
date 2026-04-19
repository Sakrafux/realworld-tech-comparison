# Load & Performance Tests

This directory contains scripts for benchmarking the performance and scalability of the various backend implementations.

## Purpose
The goal is to provide empirical data on how different tech stacks (e.g., Java vs. Go vs. Node.js) handle high traffic, focusing on:
- Latency (Response times)
- Throughput (Requests per second)
- Resource usage (CPU/RAM)

## Suggested Tech Stack
- **k6**: Modern, developer-centric load testing tool using JavaScript.
- **Prometheus/Grafana**: For monitoring and visualizing results during the test runs.
