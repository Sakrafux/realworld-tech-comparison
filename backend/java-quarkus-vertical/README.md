# RealWorld Backend: Java Quarkus (Vertical Slice Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java and Quarkus.

## Architecture

This implementation follows a **Vertical Slice Architecture** (Package-by-Feature) combined with the **Active Record Pattern**:

- **Feature-Centric**: Each package (e.g., `article`, `user`, `comment`) contains all the components required for that specific feature, including REST Resources, Services, Mappers, and Entities.
- **Active Record (Panache)**: By leveraging **Hibernate Panache**, business entities extend `PanacheEntityBase`. This combines data and persistence logic, significantly reducing boilerplate code while maintaining high performance.
- **High Cohesion**: Business logic, data access, and API definitions for a single domain are kept together, making features easier to find and modify in isolation.
- **Internal Simplicity**: Because the slices are granular, there is no internal sub-layering. Resources, services, and entities live side-by-side for maximum visibility and developer productivity.
- **Cross-Cutting Concerns**: Shared infrastructure (Security, Global Exceptions, Configurations, Base Entities) resides in a specialized `core` package.

## Tech Stack

- **Java 25**
- **Quarkus 3.35.2**
- **Hibernate Panache** (Active Record Pattern)
- **H2 / PostgreSQL** (depending on configuration)
- **Maven**
- **Monitoring**: Micrometer Registry OTLP, OpenTelemetry, Prometheus & Grafana integration
- **Security**: Stateless JWT Authentication via SmallRye JWT

## Directory Structure (Current: Granular Slices)

In this approach, slices are kept as small as possible to minimize complexity within a single package.

```text
src/
└── main/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── article/      # Article feature (Entity, Resource, Service, Mapper)
    │       ├── comment/      # Comment feature
    │       ├── user/         # User & Profile feature
    │       └── core/         # Shared infrastructure (Security, Exceptions)
    └── resources/            # application.yml
```

## Testing

This project prioritizes the global **API Test Suite** (located in the root `/test/api` directory) as the primary verification tool for specification compliance. 

The module contains a comprehensive suite of **Integration Tests** (`*IT.java`) using `@QuarkusTest`. These tests verify the interaction between Resources, Services, and Panache Entities. Native build verification is supported via `@QuarkusIntegrationTest`.

## Building

In order for the docker containers to be built, the jar (or native image) must first be built. Because Quarkus 
locks the DB at build time, this cannot be overwritten using environment variables. The same goes for OpenTelemetry.

Overall, one must be careful with Quarkus and build/run time settings.

### JVM

```shell
./mvnw clean package "-Dquarkus.datasource.db-kind=postgresql" "-Dquarkus.otel.enabled=true" -DskipTests
```

### GraalVM

```shell
...
```

## JVM Performance

- Max CPU Utilization: 12.7%
- Max Memory Usage: 624 MiB
- Max Requests per Second: 943 / 949

![performance-jvm.png](performance-jvm.png)

### API test suite

- On startup: 2.32s
- After 10 warm-up runs: 1.12s
- After load test: 1.05s

### Load test suite

#### Light load (10 VUs / 30s)

    HTTP
    http_req_duration..............: avg=6.06ms  min=570.32µs med=3.47ms   max=169.73ms p(90)=6.49ms  p(95)=10.15ms
      { expected_response:true }...: avg=6ms     min=570.32µs med=3.45ms   max=169.73ms p(90)=6.39ms  p(95)=9.87ms 
      { name:AddComment }..........: avg=4.83ms  min=2.96ms   med=4.21ms   max=20.67ms  p(90)=6.24ms  p(95)=7.89ms 
      { name:CreateArticle }.......: avg=7.81ms  min=3.92ms   med=5.64ms   max=124.86ms p(90)=9.21ms  p(95)=16.15ms
      { name:DeleteArticle }.......: avg=3.68ms  min=2.32ms   med=3.25ms   max=40.9ms   p(90)=4.45ms  p(95)=5.18ms 
      { name:DeleteComment }.......: avg=3.45ms  min=2.27ms   med=3.09ms   max=11.3ms   p(90)=4.73ms  p(95)=6.15ms 
      { name:FavoriteArticle }.....: avg=4.96ms  min=3.25ms   med=4.36ms   max=23.96ms  p(90)=6.33ms  p(95)=8.37ms 
      { name:FollowUser }..........: avg=4.98ms  min=2.53ms   med=3.59ms   max=78.1ms   p(90)=6.61ms  p(95)=8.73ms 
      { name:GetArticle }..........: avg=2.01ms  min=1.24ms   med=1.79ms   max=10.1ms   p(90)=2.48ms  p(95)=3.19ms 
      { name:GetArticlesFeed }.....: avg=1.47ms  min=778.53µs med=1.21ms   max=10.79ms  p(90)=1.79ms  p(95)=2.63ms 
      { name:GetComments }.........: avg=2ms     min=1.01ms   med=1.62ms   max=9.82ms   p(90)=2.56ms  p(95)=3.8ms  
      { name:GetCurrentUser }......: avg=1.89ms  min=1.29ms   med=1.76ms   max=3.4ms    p(90)=2.29ms  p(95)=2.46ms 
      { name:GetGlobalArticles }...: avg=5.45ms  min=3.16ms   med=4.64ms   max=25.49ms  p(90)=6.82ms  p(95)=9.78ms 
      { name:GetProfile }..........: avg=1.83ms  min=1ms      med=1.43ms   max=11.2ms   p(90)=2.43ms  p(95)=3.71ms 
      { name:GetTags }.............: avg=1.16ms  min=570.32µs med=938.78µs max=8.64ms   p(90)=1.5ms   p(95)=1.96ms 
      { name:Login }...............: avg=85.42ms min=77.54ms  med=79.49ms  max=169.73ms p(90)=94.25ms p(95)=114.9ms
      { name:Register }............: avg=86.03ms min=78.53ms  med=82.15ms  max=160.64ms p(90)=89.28ms p(95)=96.09ms
      { name:UnfavoriteArticle }...: avg=4.99ms  min=3.01ms   med=4.21ms   max=39.94ms  p(90)=6.69ms  p(95)=8.21ms 
      { name:UnfollowUser }........: avg=4.34ms  min=2.44ms   med=3.47ms   max=36.57ms  p(90)=5.73ms  p(95)=9.11ms 
    http_req_failed................: 0.25%  6 out of 2397
    http_reqs......................: 2397   75.266088/s

    EXECUTION
    iteration_duration.............: avg=1.04s   min=1s       med=1.04s    max=1.2s     p(90)=1.08s   p(95)=1.09s  
    iterations.....................: 292    9.168835/s
    vus............................: 10     min=10        max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 1.3 MB 40 kB/s
    data_sent......................: 862 kB 27 kB/s

#### Medium load (50 VUs / 1m)

    HTTP
    http_req_duration..............: avg=6.89ms  min=343.11µs med=2.51ms   max=614.76ms p(90)=5.54ms  p(95)=13.1ms  
      { expected_response:true }...: avg=6.89ms  min=343.11µs med=2.51ms   max=614.76ms p(90)=5.54ms  p(95)=13.12ms 
      { name:AddComment }..........: avg=3.82ms  min=1.87ms   med=3.15ms   max=54.66ms  p(90)=4.7ms   p(95)=6.43ms  
      { name:CreateArticle }.......: avg=7.29ms  min=2.6ms    med=4.2ms    max=500.95ms p(90)=7.12ms  p(95)=9.65ms  
      { name:DeleteArticle }.......: avg=2.99ms  min=1.56ms   med=2.44ms   max=26.72ms  p(90)=4.17ms  p(95)=5.85ms  
      { name:DeleteComment }.......: avg=3.18ms  min=1.39ms   med=2.21ms   max=389.74ms p(90)=3.3ms   p(95)=4.74ms  
      { name:FavoriteArticle }.....: avg=4.11ms  min=1.82ms   med=3.15ms   max=442.57ms p(90)=5.2ms   p(95)=7.15ms  
      { name:FollowUser }..........: avg=3.31ms  min=1.35ms   med=2.59ms   max=70.05ms  p(90)=4.14ms  p(95)=6.2ms   
      { name:GetArticle }..........: avg=2.61ms  min=635.82µs med=1.29ms   max=402.72ms p(90)=2.01ms  p(95)=3.15ms  
      { name:GetArticlesFeed }.....: avg=1.15ms  min=453.11µs med=804.68µs max=20.07ms  p(90)=1.49ms  p(95)=2.8ms   
      { name:GetComments }.........: avg=1.85ms  min=626.42µs med=1.14ms   max=403.6ms  p(90)=1.82ms  p(95)=3.09ms  
      { name:GetCurrentUser }......: avg=1.55ms  min=612.62µs med=1.14ms   max=18.63ms  p(90)=2.24ms  p(95)=3.84ms  
      { name:GetGlobalArticles }...: avg=4.04ms  min=1.89ms   med=3.48ms   max=25.5ms   p(90)=5.5ms   p(95)=7.66ms  
      { name:GetProfile }..........: avg=2.24ms  min=524.62µs med=1.04ms   max=459.4ms  p(90)=1.93ms  p(95)=2.82ms  
      { name:GetTags }.............: avg=1.16ms  min=343.11µs med=608.12µs max=431.18ms p(90)=1.07ms  p(95)=1.7ms   
      { name:Login }...............: avg=95.12ms min=76.26ms  med=79.69ms  max=561.85ms p(90)=98.99ms p(95)=160.08ms
      { name:Register }............: avg=92.14ms min=78.16ms  med=81.81ms  max=614.76ms p(90)=94.2ms  p(95)=113.51ms
      { name:UnfavoriteArticle }...: avg=4.01ms  min=1.88ms   med=3.05ms   max=428.71ms p(90)=5.17ms  p(95)=7.37ms  
      { name:UnfollowUser }........: avg=4.14ms  min=917.64µs med=2.46ms   max=481.7ms  p(90)=4.07ms  p(95)=5.8ms   
    http_req_failed................: 0.07%  13 out of 18569
    http_reqs......................: 18569  299.921801/s

    EXECUTION
    iteration_duration.............: avg=1.04s   min=1s       med=1.02s    max=1.63s    p(90)=1.08s   p(95)=1.09s   
    iterations.....................: 2897   46.791613/s
    vus............................: 47     min=47          max=50
    vus_max........................: 50     min=50          max=50

    NETWORK
    data_received..................: 10 MB  162 kB/s
    data_sent......................: 6.6 MB 107 kB/s

#### Heavy load (200 VUs / 3m)

    HTTP
    http_req_duration..............: avg=205.38ms min=305.41µs med=13.08ms  max=59.99s p(90)=104.52ms p(95)=324.09ms
      { expected_response:true }...: avg=94.09ms  min=305.41µs med=12.99ms  max=38.53s p(90)=101.17ms p(95)=300.23ms
      { name:AddComment }..........: avg=88.2ms   min=1.69ms   med=19.1ms   max=59.99s p(90)=88.59ms  p(95)=146.94ms
      { name:CreateArticle }.......: avg=172.41ms min=1.96ms   med=29.23ms  max=59.99s p(90)=142.73ms p(95)=241.8ms 
      { name:DeleteArticle }.......: avg=98.23ms  min=1.42ms   med=12.34ms  max=59.99s p(90)=58.57ms  p(95)=100.93ms
      { name:DeleteComment }.......: avg=70.74ms  min=1.09ms   med=11.34ms  max=59.99s p(90)=48.72ms  p(95)=85.15ms 
      { name:FavoriteArticle }.....: avg=109.78ms min=1.77ms   med=15.63ms  max=59.99s p(90)=65.82ms  p(95)=120.65ms
      { name:FollowUser }..........: avg=577.33ms min=775.83µs med=11.69ms  max=59.99s p(90)=50.04ms  p(95)=86.52ms 
      { name:GetArticle }..........: avg=97ms     min=674.72µs med=7.56ms   max=49.1s  p(90)=64.56ms  p(95)=160.66ms
      { name:GetArticlesFeed }.....: avg=71.42ms  min=380.81µs med=8ms      max=52.59s p(90)=49.45ms  p(95)=90.7ms  
      { name:GetComments }.........: avg=111.45ms min=551.42µs med=6.51ms   max=59.99s p(90)=43.39ms  p(95)=87ms    
      { name:GetCurrentUser }......: avg=373.71ms min=508.62µs med=10.95ms  max=59.99s p(90)=223.49ms p(95)=428.25ms
      { name:GetGlobalArticles }...: avg=171.68ms min=1.88ms   med=16.51ms  max=59.99s p(90)=89.52ms  p(95)=191.03ms
      { name:GetProfile }..........: avg=58.65ms  min=526.22µs med=7.42ms   max=59.99s p(90)=46.78ms  p(95)=82.34ms 
      { name:GetTags }.............: avg=63.75ms  min=305.41µs med=5.02ms   max=59.99s p(90)=36.01ms  p(95)=68.3ms  
      { name:Login }...............: avg=1.52s    min=77.61ms  med=994.89ms max=59.99s p(90)=1.63s    p(95)=1.8s    
      { name:Register }............: avg=1.86s    min=79.05ms  med=1.01s    max=59.99s p(90)=1.65s    p(95)=1.8s    
      { name:UnfavoriteArticle }...: avg=116.44ms min=1.72ms   med=15.8ms   max=59.99s p(90)=67.78ms  p(95)=110.17ms
      { name:UnfollowUser }........: avg=474.63ms min=955.14µs med=11.72ms  max=59.99s p(90)=52.41ms  p(95)=93.14ms 
    http_req_failed................: 0.55%  582 out of 105277
    http_reqs......................: 105277 499.319939/s

    EXECUTION
    iteration_duration.............: avg=2.21s    min=1s       med=1.18s    max=1m39s  p(90)=2.3s     p(95)=2.64s   
    iterations.....................: 17406  82.555191/s
    vus............................: 30     min=0             max=200
    vus_max........................: 200    min=200           max=200

    NETWORK
    data_received..................: 57 MB  271 kB/s
    data_sent......................: 37 MB  177 kB/s

> This load test breaks the application → doesn't work afterward

## GraalVM Performance

- Max CPU Utilization: -
- Max Memory Usage: -
- Max Requests per Second: -

<!-- Performance chart placeholder -->

### API test suite

- On startup: -
- After 10 warm-up runs: -
- After load test: -

### Load test suite

#### Light load (10 VUs / 30s)

    TBD

#### Medium load (50 VUs / 1m)

    TBD

#### Heavy load (200 VUs / 3m)

    TBD
