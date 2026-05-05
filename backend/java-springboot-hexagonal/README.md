# RealWorld Backend: Java Spring Boot (Hexagonal Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java, Spring Boot, and **Hexagonal Architecture** (also known as Ports and Adapters).

## Architecture

This implementation strictly separates business logic from technical concerns using the Hexagonal pattern:

- **Domain Layer**: The core of the application. Contains pure Java POJOs (Models) and business logic. It has zero dependencies on external frameworks or other layers.
- **Application Layer**: Orchestrates domain objects to fulfill use cases.
    - **Input Ports (Use Cases/Queries)**: Interfaces defining what the application can do.
    - **Output Ports**: Interfaces defining what the application needs (e.g., persistence, security).
    - **Application Services**: Implementations of input ports that coordinate business logic.
- **Infrastructure Layer**: Technical details and framework-specific code.
    - **Adapters (In/Out)**: Connect the application to the outside world (REST controllers, JPA repositories, security providers).
    - **Mappers**: Translate data between Domain Models, Web DTOs, and JPA Entities.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Data JPA**
- **H2 / PostgreSQL** (depending on configuration)
- **Maven**
- **MapStruct**: For type-safe mapping between layers
- **Monitoring**: Spring Boot Actuator with Micrometer & Prometheus integration
- **Security**: Stateless JWT Authentication

## Directory Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com.sakrafux.realworld/
│   │       ├── domain/             # Core business models and exceptions
│   │       ├── application/        # Use cases, queries, and orchestration
│   │       │   ├── port/in         # Driving Ports (API for the application)
│   │       │   ├── port/out        # Driven Ports (SPI for infrastructure)
│   │       │   └── service         # Orchestration logic
│   │       └── infrastructure/     # Technical details
│   │           ├── adapter/in/web  # REST Controllers and Web DTOs
│   │           ├── adapter/out/persistence # JPA Entities and Repositories
│   │           ├── configuration   # Spring framework setup
│   │           └── security        # JWT and Auth implementations
│   └── resources/                  # application.yml
└── test/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── domain/             # Logic tests for business models
    │       ├── application/        # Use Case orchestration tests (Mocking ports)
    │       └── infrastructure/     # Adapter and Mapper tests
    └── resources/                  # application.yml for testing
```

## Performance

- Max CPU Utilization: 7.04%
- Max Memory Usage: 502 MiB
- Max Requests per Second: 246 / 209

![performance.png](performance.png)

### API test suite

- On startup: 3.29s
- After 10 warm-up runs: 1.49s
- After load test: 1.13s

### Load test suite

#### Light load (10 VUs / 30s)

    HTTP
    http_req_duration..............: avg=9.01ms  min=1.4ms   med=6.42ms  max=182.9ms  p(90)=12.78ms p(95)=24.32ms
      { expected_response:true }...: avg=8.99ms  min=1.4ms   med=6.42ms  max=182.9ms  p(90)=12.75ms p(95)=24.09ms
      { name:AddComment }..........: avg=9.58ms  min=5.75ms  med=7.64ms  max=41.07ms  p(90)=14.49ms p(95)=23.47ms
      { name:CreateArticle }.......: avg=10.99ms min=5.98ms  med=8.81ms  max=56.72ms  p(90)=15.34ms p(95)=19.89ms
      { name:DeleteArticle }.......: avg=7.94ms  min=4.71ms  med=6.55ms  max=30.88ms  p(90)=12.44ms p(95)=15.16ms
      { name:DeleteComment }.......: avg=6.89ms  min=4ms     med=5.57ms  max=33.4ms   p(90)=9.57ms  p(95)=12.9ms 
      { name:FavoriteArticle }.....: avg=10.3ms  min=6.04ms  med=8.02ms  max=59.53ms  p(90)=15.07ms p(95)=23.89ms
      { name:FollowUser }..........: avg=6.79ms  min=3.83ms  med=5.24ms  max=41.4ms   p(90)=9.09ms  p(95)=12.16ms
      { name:GetArticle }..........: avg=4.84ms  min=2.3ms   med=3.52ms  max=38.1ms   p(90)=7.38ms  p(95)=11.12ms
      { name:GetArticlesFeed }.....: avg=3.68ms  min=1.93ms  med=3.23ms  max=20.46ms  p(90)=4.92ms  p(95)=6.12ms 
      { name:GetComments }.........: avg=5.85ms  min=3.21ms  med=4.7ms   max=27.69ms  p(90)=9.32ms  p(95)=10.89ms
      { name:GetCurrentUser }......: avg=3.16ms  min=2.09ms  med=2.5ms   max=12.75ms  p(90)=4.35ms  p(95)=4.51ms 
      { name:GetGlobalArticles }...: avg=11.06ms min=6.35ms  med=9.41ms  max=49.57ms  p(90)=16.04ms p(95)=19.54ms
      { name:GetProfile }..........: avg=3.47ms  min=2.01ms  med=2.98ms  max=17.52ms  p(90)=4.63ms  p(95)=5.18ms 
      { name:GetTags }.............: avg=2.67ms  min=1.4ms   med=2.17ms  max=8.66ms   p(90)=4.49ms  p(95)=5.56ms 
      { name:Login }...............: avg=68.88ms min=61.47ms med=62.95ms max=182.9ms  p(90)=71.77ms p(95)=72.08ms
      { name:Register }............: avg=70.56ms min=63.5ms  med=66.36ms max=127.88ms p(90)=77.89ms p(95)=83.31ms
      { name:UnfavoriteArticle }...: avg=9.74ms  min=5.9ms   med=7.86ms  max=35.83ms  p(90)=15.86ms p(95)=21.66ms
      { name:UnfollowUser }........: avg=5.97ms  min=3.64ms  med=5.07ms  max=24.95ms  p(90)=7.91ms  p(95)=10.35ms
    http_req_failed................: 0.08%  2 out of 2331
    http_reqs......................: 2331   73.339991/s

    EXECUTION
    iteration_duration.............: avg=1.07s   min=1s      med=1.06s   max=1.31s    p(90)=1.1s    p(95)=1.15s  
    iterations.....................: 286    8.998386/s
    vus............................: 9      min=9         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 1.8 MB 58 kB/s
    data_sent......................: 636 kB 20 kB/s

#### Medium load (50 VUs / 1m)

    HTTP
    http_req_duration..............: avg=91.53ms  min=858.74µs med=6.33ms  max=19.8s    p(90)=31.08ms  p(95)=67.01ms 
      { expected_response:true }...: avg=91.57ms  min=858.74µs med=6.33ms  max=19.8s    p(90)=30.99ms  p(95)=66.99ms 
      { name:AddComment }..........: avg=17.06ms  min=4.01ms   med=7.3ms   max=315.98ms p(90)=27.86ms  p(95)=55.01ms 
      { name:CreateArticle }.......: avg=467.88ms min=4.68ms   med=8.7ms   max=19.8s    p(90)=30.67ms  p(95)=68.36ms 
      { name:DeleteArticle }.......: avg=11.41ms  min=3.14ms   med=6.2ms   max=180.97ms p(90)=22.34ms  p(95)=35.31ms 
      { name:DeleteComment }.......: avg=9.92ms   min=2.64ms   med=5.28ms  max=133.91ms p(90)=20.57ms  p(95)=28.7ms  
      { name:FavoriteArticle }.....: avg=14.98ms  min=3.57ms   med=7.69ms  max=327.29ms p(90)=28.62ms  p(95)=41.59ms 
      { name:FollowUser }..........: avg=10.89ms  min=2.15ms   med=4.86ms  max=281.06ms p(90)=15.02ms  p(95)=27.54ms 
      { name:GetArticle }..........: avg=11.21ms  min=1.37ms   med=3.36ms  max=447.3ms  p(90)=13.78ms  p(95)=27.59ms 
      { name:GetArticlesFeed }.....: avg=6.5ms    min=1.22ms   med=3.01ms  max=169.84ms p(90)=12.26ms  p(95)=22.32ms 
      { name:GetComments }.........: avg=9.66ms   min=2.19ms   med=4.12ms  max=199.54ms p(90)=18.53ms  p(95)=31.45ms 
      { name:GetCurrentUser }......: avg=7.27ms   min=1.02ms   med=2.3ms   max=281.89ms p(90)=7.76ms   p(95)=19.1ms  
      { name:GetGlobalArticles }...: avg=15.4ms   min=5.11ms   med=9.28ms  max=177.89ms p(90)=30.3ms   p(95)=43.72ms 
      { name:GetProfile }..........: avg=243.13ms min=1.13ms   med=2.88ms  max=19.7s    p(90)=10.61ms  p(95)=21.33ms 
      { name:GetTags }.............: avg=5.06ms   min=858.74µs med=2.15ms  max=108.07ms p(90)=9.93ms   p(95)=18.36ms 
      { name:Login }...............: avg=585.56ms min=60.95ms  med=70.27ms max=19.69s   p(90)=179.49ms p(95)=310.65ms
      { name:Register }............: avg=544.49ms min=62.65ms  med=70.13ms max=19.57s   p(90)=148.44ms p(95)=231.61ms
      { name:UnfavoriteArticle }...: avg=58.66ms  min=3.76ms   med=7.76ms  max=19.64s   p(90)=29.11ms  p(95)=54.8ms  
      { name:UnfollowUser }........: avg=8.91ms   min=2.41ms   med=4.81ms  max=247.79ms p(90)=14.65ms  p(95)=21.63ms 
    http_req_failed................: 0.07%  10 out of 12606
    http_reqs......................: 12606  196.911982/s

    EXECUTION
    iteration_duration.............: avg=1.57s    min=1s       med=1.06s   max=21.18s   p(90)=1.2s     p(95)=1.41s   
    iterations.....................: 2000   31.240993/s
    vus............................: 17     min=17          max=50
    vus_max........................: 50     min=50          max=50

    NETWORK
    data_received..................: 10 MB  156 kB/s
    data_sent......................: 3.4 MB 53 kB/s

#### Heavy load (200 VUs / 3m)

    HTTP
    http_req_duration..............: avg=2.12s    min=701.53µs med=328.74ms max=34.23s p(90)=933.76ms p(95)=29.41s  
      { expected_response:true }...: avg=2.11s    min=701.53µs med=328.73ms max=34.23s p(90)=933.55ms p(95)=29.29s  
      { name:AddComment }..........: avg=1.52s    min=3.37ms   med=320.29ms max=33.48s p(90)=799.51ms p(95)=1.13s   
      { name:CreateArticle }.......: avg=5.36s    min=5.09ms   med=393.74ms max=33.82s p(90)=30.03s   p(95)=32.05s  
      { name:DeleteArticle }.......: avg=1.07s    min=3.01ms   med=299.72ms max=33.73s p(90)=783.13ms p(95)=998.73ms
      { name:DeleteComment }.......: avg=1.52s    min=2.69ms   med=309.19ms max=33.5s  p(90)=817.5ms  p(95)=1.27s   
      { name:FavoriteArticle }.....: avg=1.1s     min=4.13ms   med=315.96ms max=33.53s p(90)=812.33ms p(95)=1.04s   
      { name:FollowUser }..........: avg=932.74ms min=1.42ms   med=308.87ms max=33.29s p(90)=750.11ms p(95)=930.3ms 
      { name:GetArticle }..........: avg=801.73ms min=1.27ms   med=303.63ms max=33.07s p(90)=761.32ms p(95)=938.63ms
      { name:GetArticlesFeed }.....: avg=1.17s    min=1.3ms    med=293.4ms  max=33.27s p(90)=796.53ms p(95)=1.1s    
      { name:GetComments }.........: avg=1.26s    min=2.34ms   med=278.36ms max=33.32s p(90)=759.39ms p(95)=932.65ms
      { name:GetCurrentUser }......: avg=1.08s    min=858.44µs med=311.93ms max=33.07s p(90)=781.97ms p(95)=1.05s   
      { name:GetGlobalArticles }...: avg=1.36s    min=7.23ms   med=319.59ms max=33.65s p(90)=793.09ms p(95)=1.08s   
      { name:GetProfile }..........: avg=4.5s     min=1.15ms   med=354.71ms max=33.96s p(90)=29.81s   p(95)=31.97s  
      { name:GetTags }.............: avg=1.21s    min=701.53µs med=272.87ms max=33.33s p(90)=698.59ms p(95)=893.18ms
      { name:Login }...............: avg=5.58s    min=75.24ms  med=580.98ms max=34.22s p(90)=31.21s   p(95)=32.34s  
      { name:Register }............: avg=5.34s    min=64.92ms  med=582.61ms max=34.23s p(90)=30.93s   p(95)=32.2s   
      { name:UnfavoriteArticle }...: avg=1.3s     min=3.41ms   med=299.41ms max=33.3s  p(90)=808.39ms p(95)=1.18s   
      { name:UnfollowUser }........: avg=1.29s    min=2.17ms   med=296.66ms max=33.29s p(90)=766.96ms p(95)=1.05s   
    http_req_failed................: 0.05%  9 out of 15854
    http_reqs......................: 15854  84.584892/s

    EXECUTION
    iteration_duration.............: avg=10.41s   min=1s       med=2.59s    max=39.91s p(90)=34.19s   p(95)=35.42s  
    iterations.....................: 3574   19.068147/s
    vus............................: 104    min=104        max=200
    vus_max........................: 200    min=200        max=200

    NETWORK
    data_received..................: 13 MB  67 kB/s
    data_sent......................: 4.3 MB 23 kB/s
