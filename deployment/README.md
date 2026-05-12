# Deployment & Orchestration

This directory contains the configurations necessary to spin up different versions of the RealWorld stack.

## Orchestration Strategy

While individual `docker-compose.yml` files for each stack are the baseline, the "better" approach for a comparison repo is a **Matrix Orchestrator**.

### The "Matrix" Approach
Since any Frontend should work with any Backend, we use a modular approach powered by **Docker Compose Profiles**.

Each implementation (backend or frontend) and infrastructure component (monitoring) is assigned to a profile. This allows you to mix and match them without needing complex scripts or multiple compose files.

#### Running the Stack
Use the `-f` flag to enable the parts of the stack you want to run. 

> [!IMPORTANT]  
> The `-f` flag must be placed **before** the command (e.g., `up`, `ps`, `config`).

```bash
# Run the Java Backend alone
docker-compose -f docker-compose.yml -f stacks/java-springboot-layered.yml up -d

# Run the Java Backend + Monitoring
docker-compose -f docker-compose.yml -f stacks/java-springboot-layered.yml -f monitoring.yml up -d
```

## Structure
- `stacks/`: Specific `docker-compose` fragments using profiles.
- `scripts/`: Helper scripts for seeding databases or cleaning up volumes.
- `env/`: Pre-configured environment variables for different scenarios.

## Monitoring via Grafana

If a configuration is started with `-f monitoring.yml`, then Grafana is available on http://localhost:4000 with the pre-configured dashboard `Server Overview`.

## All setups

```bash
docker-compose -f docker-compose.yml -f stacks/java-springboot-layered.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-springboot-vertical.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-springboot-hexagonal.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-springboot-hive.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-quarkus-vertical-jvm.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-quarkus-vertical-graalvm.yml up --build
docker-compose -f docker-compose.yml -f stacks/go-chi-hexagonal.yml up --build
docker-compose -f docker-compose.yml -f stacks/go-chi-hive.yml up --build
```

### With Monitoring

```bash
docker-compose -f docker-compose.yml -f stacks/java-springboot-layered.yml -f stacks/monitoring.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-springboot-vertical.yml -f stacks/monitoring.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-springboot-hexagonal.yml -f stacks/monitoring.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-springboot-hive.yml -f stacks/monitoring.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-quarkus-vertical-jvm.yml -f stacks/monitoring.yml up --build
docker-compose -f docker-compose.yml -f stacks/java-quarkus-vertical-graalvm.yml -f stacks/monitoring.yml up --build
docker-compose -f docker-compose.yml -f stacks/go-chi-hexagonal.yml -f stacks/monitoring.yml up --build
docker-compose -f docker-compose.yml -f stacks/go-chi-hive.yml -f stacks/monitoring.yml up --build
```
