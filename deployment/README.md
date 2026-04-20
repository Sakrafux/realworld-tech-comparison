# Deployment & Orchestration

This directory contains the configurations necessary to spin up different versions of the RealWorld stack.

## Orchestration Strategy

While individual `docker-compose.yml` files for each stack are the baseline, the "better" approach for a comparison repo is a **Matrix Orchestrator**.

### The "Matrix" Approach
Since any Frontend should work with any Backend, we use a modular approach powered by **Docker Compose Profiles**.

Each implementation (backend or frontend) and infrastructure component (monitoring) is assigned to a profile. This allows you to mix and match them without needing complex scripts or multiple compose files.

#### Running the Stack
Use the `--profile` flag to enable the parts of the stack you want to run. 

> [!IMPORTANT]  
> The `--profile` flag must be placed **before** the command (e.g., `up`, `ps`, `config`).

```bash
# Run the Java Backend alone
docker-compose --profile java-springboot-layered up -d

# Run the Java Backend + Monitoring
docker-compose --profile java-springboot-layered --profile monitoring up -d
```

Alternatively, use the `COMPOSE_PROFILES` environment variable:
```bash
COMPOSE_PROFILES=java-springboot-layered,monitoring docker-compose up -d
```

## Structure
- `stacks/`: Specific `docker-compose` fragments using profiles.
- `scripts/`: Helper scripts for seeding databases or cleaning up volumes.
- `env/`: Pre-configured environment variables for different scenarios.
