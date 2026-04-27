# RealWorld Backend: Java Spring Boot ("Hive" - Vertical Hexagonal)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java, Spring Boot, and **Hive Architecture**. Hive (Vertical Hexagonal) merges the strict isolation of Hexagonal Architecture with the high cohesion of Vertical Slice Architecture.

## Architecture

This implementation organizes the codebase into self-contained **Cells** (Feature Packages) that encapsulate their own Hexagonal layers.

### The Hive Principles
- **Feature-Isolated Cells**: The top-level package organization is by business feature (e.g., `user`, `article`). Each cell is a self-contained "Hexagon" responsible for its own business logic, API endpoints, and persistence.
- **Internal Hexagon**: Within each cell, code is structured into `domain`, `application`, and `infrastructure` layers, maintaining the dependency rule: Inwards towards the Domain.
- **Port-Based Communication (Internal APIs)**: Cells are forbidden from touching each other's internal ports or adapters. Cross-cell interaction MUST happen only through specialized **Internal API Ports** (located in `application.port.api`).
- **Persistence Decoupling**: Cells do not share JPA entities or repositories. Relationships between cells (e.g., Article to Author) are managed using **IDs** and resolved through the Internal APIs, ensuring database-level isolation.
- **Shared Core**: Cross-cutting concerns like security (JWT), global exception handling, and shared configurations reside in a central `core` package.

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
src/main/java/com/sakrafux/realworld/
├── article/                # --- ARTICLE CELL ---
│   ├── domain/             # Models (Article, Comment, Author)
│   ├── application/        # Orchestration logic
│   │   ├── port/in         # Driving Ports (Use Cases)
│   │   ├── port/out        # Driven Ports (Repositories)
│   │   ├── port/api        # Internal Ports for other cells
│   │   └── service/        # Use Case implementations
│   └── infrastructure/     # Technical details
│       ├── web/            # REST Controllers and DTOs
│       └── persistence/    # JPA Entities (ID-based links) and Repositories
│
├── user/                   # --- USER CELL ---
│   ├── domain/             # Models (User, Profile)
│   ├── application/ 
│   │   ├── port/in         
│   │   ├── port/out        
│   │   ├── port/api 
│   │   └── service/        
│   └── infrastructure/     
│       ├── web/            
│       └── persistence/    
│
├── core/                   # --- SHARED CORE ---
│   ├── configuration/      # Global Spring Setup
│   ├── exception/          # Global Exception Handling
│   ├── security/           # JWT & Auth Filters
│   └── persistence/        # BaseEntity & Shared JPA logic
│
└── RealworldApplication.java
```

## Performance

![performance.png](performance.png)

### API test suite
*Placeholder for results*
- On startup: 3.39s
- After 10 warm-up runs: 1.30s
- After load test: 1.04s

### Load test suite

#### Light load (10 VUs / 30s)

    HTTP
    http_req_duration..............: avg=23.77ms min=1.21ms  med=7.18ms  max=405.37ms p(90)=68.89ms  p(95)=92.86ms 
      { expected_response:true }...: avg=23.77ms min=1.21ms  med=7.18ms  max=405.37ms p(90)=68.89ms  p(95)=92.86ms 
      { name:AddComment }..........: avg=10.59ms min=4.23ms  med=8.14ms  max=67.18ms  p(90)=17.9ms   p(95)=26.95ms 
      { name:CreateArticle }.......: avg=13.08ms min=5.11ms  med=8.39ms  max=121.08ms p(90)=24.58ms  p(95)=31.38ms 
      { name:DeleteArticle }.......: avg=8.6ms   min=3.46ms  med=6.08ms  max=53.59ms  p(90)=15.88ms  p(95)=21.41ms 
      { name:DeleteComment }.......: avg=8.49ms  min=3.42ms  med=5.81ms  max=61.64ms  p(90)=15.44ms  p(95)=19.63ms 
      { name:FavoriteArticle }.....: avg=9.74ms  min=4.02ms  med=6.95ms  max=35.85ms  p(90)=18.66ms  p(95)=22.69ms 
      { name:FollowUser }..........: avg=6.94ms  min=3.26ms  med=5.34ms  max=35.34ms  p(90)=11.5ms   p(95)=14.07ms 
      { name:GetArticle }..........: avg=5.8ms   min=2.25ms  med=3.8ms   max=52.54ms  p(90)=10.83ms  p(95)=13.82ms 
      { name:GetArticlesFeed }.....: avg=4.6ms   min=1.67ms  med=3.19ms  max=30.35ms  p(90)=9.2ms    p(95)=11.59ms 
      { name:GetComments }.........: avg=5.85ms  min=2.17ms  med=4.11ms  max=32.09ms  p(90)=10.53ms  p(95)=14.96ms 
      { name:GetCurrentUser }......: avg=4.05ms  min=1.45ms  med=2.54ms  max=55.67ms  p(90)=8.38ms   p(95)=10.69ms 
      { name:GetGlobalArticles }...: avg=24.68ms min=11.35ms med=17.88ms max=90.08ms  p(90)=42.47ms  p(95)=62.65ms 
      { name:GetTags }.............: avg=3.72ms  min=1.21ms  med=2.31ms  max=18.65ms  p(90)=8.21ms   p(95)=9.11ms  
      { name:Login }...............: avg=93.9ms  min=60.5ms  med=69.58ms max=377.63ms p(90)=168.59ms p(95)=208.98ms
      { name:Register }............: avg=93.71ms min=61.89ms med=72.23ms max=405.37ms p(90)=150.57ms p(95)=192.77ms
      { name:UnfavoriteArticle }...: avg=10.14ms min=4.19ms  med=6.92ms  max=46.73ms  p(90)=18.82ms  p(95)=24.34ms 
      { name:UnfollowUser }........: avg=6.53ms  min=3.1ms   med=5.26ms  max=24.42ms  p(90)=11.14ms  p(95)=14.46ms 
    http_req_failed................: 0.00%  0 out of 3706
    http_reqs......................: 3706   118.610557/s

    EXECUTION
    iteration_duration.............: avg=1.4s    min=1.25s   med=1.32s   max=2.16s    p(90)=1.65s    p(95)=1.84s   
    iterations.....................: 218    6.977092/s
    vus............................: 4      min=4         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 3.1 MB 99 kB/s
    data_sent......................: 966 kB 31 kB/s

#### Medium load (50 VUs / 1m)

    HTTP
    http_req_duration..............: avg=154.49ms min=696.43µs med=96.65ms  max=2.03s    p(90)=379.68ms p(95)=526.62ms
      { expected_response:true }...: avg=154.49ms min=696.43µs med=96.65ms  max=2.03s    p(90)=379.68ms p(95)=526.62ms
      { name:AddComment }..........: avg=120.92ms min=2.82ms   med=82.73ms  max=1.08s    p(90)=288.53ms p(95)=377.79ms
      { name:CreateArticle }.......: avg=163.38ms min=3.1ms    med=114.83ms max=1.02s    p(90)=398.04ms p(95)=531.25ms
      { name:DeleteArticle }.......: avg=94.21ms  min=2.17ms   med=60.93ms  max=641.82ms p(90)=227.9ms  p(95)=310.41ms
      { name:DeleteComment }.......: avg=86.31ms  min=1.9ms    med=57.99ms  max=794.88ms p(90)=203.47ms p(95)=279.21ms
      { name:FavoriteArticle }.....: avg=102.41ms min=2.56ms   med=77.73ms  max=682.45ms p(90)=232.83ms p(95)=303.03ms
      { name:FollowUser }..........: avg=83.29ms  min=1.93ms   med=41.38ms  max=833.99ms p(90)=222.31ms p(95)=305.49ms
      { name:GetArticle }..........: avg=113.71ms min=1.23ms   med=64.16ms  max=897.03ms p(90)=304.77ms p(95)=442.18ms
      { name:GetArticlesFeed }.....: avg=101.46ms min=954.74µs med=71.18ms  max=622.89ms p(90)=256.28ms p(95)=327.14ms
      { name:GetComments }.........: avg=91.8ms   min=1.19ms   med=60.21ms  max=803.56ms p(90)=239.68ms p(95)=315.98ms
      { name:GetCurrentUser }......: avg=174.72ms min=772.67µs med=105.93ms max=1.28s    p(90)=439.42ms p(95)=604.76ms
      { name:GetGlobalArticles }...: avg=129.5ms  min=10.06ms  med=105.6ms  max=831.48ms p(90)=275.37ms p(95)=335.27ms
      { name:GetTags }.............: avg=87.12ms  min=696.43µs med=58.36ms  max=825.32ms p(90)=234.43ms p(95)=304.27ms
      { name:Login }...............: avg=386.73ms min=60.21ms  med=324.48ms max=1.85s    p(90)=743.75ms p(95)=855.72ms
      { name:Register }............: avg=361.51ms min=61.23ms  med=306.56ms max=2.03s    p(90)=624.26ms p(95)=812.59ms
      { name:UnfavoriteArticle }...: avg=89.55ms  min=2.44ms   med=61.16ms  max=679.14ms p(90)=216.28ms p(95)=293.1ms 
      { name:UnfollowUser }........: avg=78.21ms  min=1.7ms    med=35.88ms  max=804.92ms p(90)=209.68ms p(95)=266.13ms
    http_req_failed................: 0.00%  0 out of 14620
    http_reqs......................: 14620  232.67284/s

    EXECUTION
    iteration_duration.............: avg=3.63s    min=1.24s    med=3.68s    max=5.38s    p(90)=4.38s    p(95)=4.72s   
    iterations.....................: 860    13.686638/s
    vus............................: 48     min=48         max=50
    vus_max........................: 50     min=50         max=50

    NETWORK
    data_received..................: 12 MB  192 kB/s
    data_sent......................: 3.8 MB 61 kB/s

#### Heavy load (200 VUs / 3m)

    HTTP
    http_req_duration..............: avg=1.48s  min=728.93µs med=639.46ms max=23.43s p(90)=1.83s  p(95)=5.74s 
      { expected_response:true }...: avg=1.48s  min=728.93µs med=639.46ms max=23.43s p(90)=1.83s  p(95)=5.74s 
      { name:AddComment }..........: avg=1.44s  min=2.45ms   med=602.87ms max=22.56s p(90)=1.61s  p(95)=2.96s 
      { name:CreateArticle }.......: avg=1.43s  min=3.22ms   med=664.39ms max=23.32s p(90)=1.88s  p(95)=2.72s 
      { name:DeleteArticle }.......: avg=1.08s  min=2.02ms   med=539.36ms max=22.35s p(90)=1.39s  p(95)=1.94s 
      { name:DeleteComment }.......: avg=1.24s  min=1.84ms   med=551.54ms max=22.38s p(90)=1.49s  p(95)=2.47s 
      { name:FavoriteArticle }.....: avg=1.17s  min=2.81ms   med=560.29ms max=21.44s p(90)=1.47s  p(95)=2.46s 
      { name:FollowUser }..........: avg=1.28s  min=1.76ms   med=613.48ms max=22.21s p(90)=1.62s  p(95)=3.15s 
      { name:GetArticle }..........: avg=1.14s  min=1.1ms    med=578.15ms max=22.26s p(90)=1.49s  p(95)=2.18s 
      { name:GetArticlesFeed }.....: avg=1.34s  min=889.12µs med=628.64ms max=22.35s p(90)=1.54s  p(95)=3.02s 
      { name:GetComments }.........: avg=1.23s  min=1.12ms   med=520.22ms max=23.33s p(90)=1.51s  p(95)=2.26s 
      { name:GetCurrentUser }......: avg=1.35s  min=741.43µs med=659.73ms max=23.43s p(90)=1.99s  p(95)=2.93s 
      { name:GetGlobalArticles }...: avg=1.48s  min=15.44ms  med=621.28ms max=22.35s p(90)=1.67s  p(95)=6.03s 
      { name:GetTags }.............: avg=1.44s  min=728.93µs med=553.72ms max=22.66s p(90)=1.55s  p(95)=6.93s 
      { name:Login }...............: avg=1.8s   min=70.35ms  med=872.88ms max=22.67s p(90)=2.65s  p(95)=5.75s 
      { name:Register }............: avg=2.51s  min=66.81ms  med=949.83ms max=23.31s p(90)=5.49s  p(95)=18.9s 
      { name:UnfavoriteArticle }...: avg=1.3s   min=2.64ms   med=549.59ms max=21.88s p(90)=1.51s  p(95)=2.36s 
      { name:UnfollowUser }........: avg=1.4s   min=1.72ms   med=652.71ms max=22.1s  p(90)=1.76s  p(95)=2.48s 
    http_req_failed................: 0.00%  0 out of 24463
    http_reqs......................: 24463  118.147242/s

    EXECUTION
    iteration_duration.............: avg=26.24s min=5.04s    med=25.45s   max=1m14s  p(90)=50.61s p(95)=52.72s
    iterations.....................: 1439   6.949838/s
    vus............................: 15     min=15         max=200
    vus_max........................: 200    min=200        max=200

    NETWORK
    data_received..................: 20 MB  97 kB/s
    data_sent......................: 6.4 MB 31 kB/s