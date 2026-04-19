# Deployment & Orchestration

This directory contains the configurations necessary to spin up different versions of the RealWorld stack.

## Orchestration Strategy

While individual `docker-compose.yml` files for each stack are the baseline, the "better" approach for a comparison repo is a **Matrix Orchestrator**.

### The "Matrix" Approach
Since any Frontend should work with any Backend, we use a modular approach:

1.  **Environment Files**: `.env` files to switch implementation images.
2.  **Base Compose + Overrides**: Using `docker-compose -f docker-compose.yml -f docker-compose.java.yml up` to mix and match.
3.  **Makefile / CLI Script**: (Recommended) A simple script at the root or here to run commands like:
    ```bash
    # Example usage (conceptual)
    ./stack.sh up --backend java --frontend react --db postgres
    ```

## Structure
- `stacks/`: Specific `docker-compose` files or override fragments.
- `scripts/`: Helper scripts for seeding databases or cleaning up volumes.
- `env/`: Pre-configured environment variables for different scenarios.
