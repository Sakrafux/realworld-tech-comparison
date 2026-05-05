# RealWorld Backend: Java Spring Boot (Layered Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java and Spring Boot.

## Architecture

This implementation follows a classic **Layered Architecture** (also known as N-Tier Architecture):

- **Web/Controller Layer**: Handles incoming HTTP requests and maps them to service calls.
- **Service Layer**: Contains the business logic and orchestrates data flow.
- **Persistence/Repository Layer**: Manages data access and interaction with the database.
- **Domain Layer**: Defines the core entities and data models.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Data JPA**
- **H2 / PostgreSQL** (depending on configuration)
- **Maven**
- **Monitoring**: Spring Boot Actuator with Micrometer & Prometheus integration
- **Security**: Stateless JWT Authentication

## Directory Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com.sakrafux.realworld/
│   │       ├── configuration/   # Spring configuration (Security, JPA, Filters)
│   │       ├── controller/      # REST API endpoints and Global Exception Handler
│   │       ├── dto/             # Data Transfer Objects (Request/Response)
│   │       ├── entity/          # JPA Database Entities
│   │       ├── exception/       # Custom exceptions 
│   │       ├── mapper/          # MapStruct mappers (DTO <-> Entity)
│   │       ├── repository/      # Spring Data JPA Repositories
│   │       ├── security/        # JWT parsing and authentication filters
│   │       └── service/         # Core business logic
│   └── resources/               # application.yml
└── test/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── controller/      # Integration tests for REST endpoints (*IT.java)
    │       ├── security/        # Unit tests for security utilities (*Test.java)
    │       └── service/         # Unit tests for business logic (*Test.java)
    └── resources/               # application.yml for testing (in-memory H2)
```

## Performance

- Max CPU Utilization: 6.44%
- Max Memory Usage: 580 MiB
- Max Requests per Second: 243 / 223

![performance.png](performance.png)

### API test suite

- On startup: 3.27s
- After 10 warm-up runs: 1.42s
- After load test: 1.01s

### Load test suite

#### Light load (10 VUs / 30s)

    HTTP
    http_req_duration..............: avg=9.11ms  min=1.55ms  med=6.29ms  max=148.71ms p(90)=13ms    p(95)=20.96ms
      { expected_response:true }...: avg=9.1ms   min=1.55ms  med=6.29ms  max=148.71ms p(90)=12.96ms p(95)=20.61ms
      { name:AddComment }..........: avg=8.83ms  min=5.41ms  med=7.23ms  max=33.13ms  p(90)=12.53ms p(95)=16.47ms
      { name:CreateArticle }.......: avg=13.75ms min=6.42ms  med=9.23ms  max=148.71ms p(90)=14.94ms p(95)=21.81ms
      { name:DeleteArticle }.......: avg=7.02ms  min=3.99ms  med=6.08ms  max=24.93ms  p(90)=10.38ms p(95)=13.44ms
      { name:DeleteComment }.......: avg=7.6ms   min=4.08ms  med=6ms     max=36.55ms  p(90)=11.26ms p(95)=16.85ms
      { name:FavoriteArticle }.....: avg=9.24ms  min=5.06ms  med=7.86ms  max=32.26ms  p(90)=14ms    p(95)=18.25ms
      { name:FollowUser }..........: avg=6.97ms  min=3.69ms  med=5.67ms  max=30.56ms  p(90)=9.92ms  p(95)=12.9ms 
      { name:GetArticle }..........: avg=5.02ms  min=2.6ms   med=4.11ms  max=20.52ms  p(90)=7.24ms  p(95)=10.23ms
      { name:GetArticlesFeed }.....: avg=3.76ms  min=2.03ms  med=3.22ms  max=13.06ms  p(90)=5.4ms   p(95)=7.3ms  
      { name:GetComments }.........: avg=4.83ms  min=2.7ms   med=4.1ms   max=14.9ms   p(90)=7.9ms   p(95)=9.2ms  
      { name:GetCurrentUser }......: avg=3.04ms  min=2.03ms  med=2.82ms  max=6.81ms   p(90)=3.56ms  p(95)=4.35ms 
      { name:GetGlobalArticles }...: avg=11.99ms min=6.53ms  med=10.23ms max=48.05ms  p(90)=18.41ms p(95)=22.31ms
      { name:GetProfile }..........: avg=4.03ms  min=1.84ms  med=3.39ms  max=30.87ms  p(90)=5.29ms  p(95)=6.44ms 
      { name:GetTags }.............: avg=3.21ms  min=1.55ms  med=2.57ms  max=19.22ms  p(90)=4.68ms  p(95)=6.25ms 
      { name:Login }...............: avg=69.63ms min=62.53ms med=64.45ms max=115.37ms p(90)=85.31ms p(95)=90.45ms
      { name:Register }............: avg=69.98ms min=63.66ms med=67.49ms max=134.27ms p(90)=74.07ms p(95)=77.23ms
      { name:UnfavoriteArticle }...: avg=9.17ms  min=5.26ms  med=7.56ms  max=28.44ms  p(90)=15.13ms p(95)=19.37ms
      { name:UnfollowUser }........: avg=6.22ms  min=3.73ms  med=5.48ms  max=21.31ms  p(90)=8.87ms  p(95)=10.24ms
    http_req_failed................: 0.12%  3 out of 2345
    http_reqs......................: 2345   74.016266/s

    EXECUTION
    iteration_duration.............: avg=1.07s   min=1s      med=1.06s   max=1.33s    p(90)=1.12s   p(95)=1.13s  
    iterations.....................: 285    8.99558/s
    vus............................: 7      min=7         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 1.9 MB 58 kB/s
    data_sent......................: 640 kB 20 kB/s


#### Medium load (50 VUs / 1m)

    HTTP
    http_req_duration..............: avg=63.02ms  min=703.63µs med=4.92ms  max=14.55s   p(90)=21.56ms  p(95)=63.29ms 
      { expected_response:true }...: avg=63.07ms  min=703.63µs med=4.92ms  max=14.55s   p(90)=21.55ms  p(95)=63.29ms 
      { name:AddComment }..........: avg=11.26ms  min=3.12ms   med=5.61ms  max=313.92ms p(90)=16.76ms  p(95)=36.09ms 
      { name:CreateArticle }.......: avg=285.51ms min=3.78ms   med=7.12ms  max=14.3s    p(90)=22.84ms  p(95)=47.12ms 
      { name:DeleteArticle }.......: avg=18.94ms  min=2.56ms   med=4.76ms  max=14.28s   p(90)=14.19ms  p(95)=23.07ms 
      { name:DeleteComment }.......: avg=7.34ms   min=2.5ms    med=4.28ms  max=106.74ms p(90)=13.82ms  p(95)=22.47ms 
      { name:FavoriteArticle }.....: avg=24.83ms  min=2.94ms   med=5.68ms  max=14.28s   p(90)=18.6ms   p(95)=28.5ms  
      { name:FollowUser }..........: avg=9.78ms   min=2.13ms   med=4.22ms  max=408.56ms p(90)=11.79ms  p(95)=19.16ms 
      { name:GetArticle }..........: avg=20.49ms  min=1.28ms   med=2.94ms  max=14.3s    p(90)=10.33ms  p(95)=24.27ms 
      { name:GetArticlesFeed }.....: avg=18.78ms  min=948.34µs med=2.36ms  max=14.28s   p(90)=8.61ms   p(95)=13.39ms 
      { name:GetComments }.........: avg=5.86ms   min=1.4ms    med=2.93ms  max=309.03ms p(90)=9.51ms   p(95)=19.39ms 
      { name:GetCurrentUser }......: avg=41.88ms  min=914.04µs med=2.05ms  max=14.28s   p(90)=6.2ms    p(95)=12.09ms 
      { name:GetGlobalArticles }...: avg=12.57ms  min=4.7ms    med=8.21ms  max=251.56ms p(90)=22.79ms  p(95)=31.55ms 
      { name:GetProfile }..........: avg=183.36ms min=971.05µs med=2.55ms  max=14.28s   p(90)=7.8ms    p(95)=12.72ms 
      { name:GetTags }.............: avg=3.79ms   min=703.63µs med=1.88ms  max=80.93ms  p(90)=6.79ms   p(95)=11.84ms 
      { name:Login }...............: avg=374.66ms min=60.62ms  med=69.25ms max=14.29s   p(90)=129.15ms p(95)=219.1ms 
      { name:Register }............: avg=391.84ms min=62.08ms  med=67.67ms max=14.55s   p(90)=106.07ms p(95)=231.26ms
      { name:UnfavoriteArticle }...: avg=10.05ms  min=3.02ms   med=5.71ms  max=133.91ms p(90)=18.46ms  p(95)=32.9ms  
      { name:UnfollowUser }........: avg=7.54ms   min=2.25ms   med=4.14ms  max=335.84ms p(90)=9.95ms   p(95)=14.03ms 
    http_req_failed................: 0.10%  15 out of 13698
    http_reqs......................: 13698  221.959318/s

    EXECUTION
    iteration_duration.............: avg=1.4s     min=1s       med=1.05s   max=15.77s   p(90)=1.12s    p(95)=1.27s   
    iterations.....................: 2166   35.097378/s
    vus............................: 48     min=48          max=50
    vus_max........................: 50     min=50          max=50

    NETWORK
    data_received..................: 11 MB  176 kB/s
    data_sent......................: 3.7 MB 60 kB/s

#### Heavy load (200 VUs / 3m)

    HTTP
    http_req_duration..............: avg=2.41s  min=741.63µs med=253.35ms max=37.39s p(90)=882.14ms p(95)=32.13s  
      { expected_response:true }...: avg=2.39s  min=741.63µs med=253.21ms max=37.39s p(90)=875.68ms p(95)=32.09s  
      { name:AddComment }..........: avg=1.19s  min=3.02ms   med=239.01ms max=36.84s p(90)=674.58ms p(95)=964.43ms
      { name:CreateArticle }.......: avg=6.25s  min=3.57ms   med=335.99ms max=37.28s p(90)=33.37s   p(95)=34.95s  
      { name:DeleteArticle }.......: avg=1.22s  min=2.12ms   med=214.87ms max=37.01s p(90)=641.3ms  p(95)=967.94ms
      { name:DeleteComment }.......: avg=1.29s  min=2.23ms   med=219.26ms max=37.01s p(90)=622.59ms p(95)=961.06ms
      { name:FavoriteArticle }.....: avg=1.12s  min=3.18ms   med=237.39ms max=37s    p(90)=678.47ms p(95)=948ms   
      { name:FollowUser }..........: avg=1.06s  min=1.14ms   med=237.6ms  max=36.88s p(90)=686.82ms p(95)=1.04s   
      { name:GetArticle }..........: avg=1.13s  min=1.21ms   med=235.01ms max=37s    p(90)=693.58ms p(95)=1s      
      { name:GetArticlesFeed }.....: avg=1.17s  min=1ms      med=222.09ms max=37.26s p(90)=635.17ms p(95)=803.5ms 
      { name:GetComments }.........: avg=1.22s  min=1.38ms   med=206.64ms max=37.01s p(90)=656.84ms p(95)=940.44ms
      { name:GetCurrentUser }......: avg=1.25s  min=900.54µs med=229.82ms max=36.83s p(90)=635.22ms p(95)=904.87ms
      { name:GetGlobalArticles }...: avg=1.74s  min=5.73ms   med=235.59ms max=37.39s p(90)=745.01ms p(95)=3.35s   
      { name:GetProfile }..........: avg=5.66s  min=1.05ms   med=291.03ms max=37.06s p(90)=32.57s   p(95)=34.82s  
      { name:GetTags }.............: avg=1.35s  min=741.63µs med=209.49ms max=37.02s p(90)=610.18ms p(95)=871.49ms
      { name:Login }...............: avg=6.94s  min=85.41ms  med=523.58ms max=37.33s p(90)=33.72s   p(95)=35.11s  
      { name:Register }............: avg=6.64s  min=62.92ms  med=491.35ms max=37.06s p(90)=33.6s    p(95)=35.12s  
      { name:UnfavoriteArticle }...: avg=1.16s  min=3.24ms   med=214.28ms max=37s    p(90)=638.12ms p(95)=905.74ms
      { name:UnfollowUser }........: avg=1.17s  min=2.11ms   med=220.43ms max=37.12s p(90)=615.52ms p(95)=761.42ms
    http_req_failed................: 0.17%  27 out of 15053
    http_reqs......................: 15053  75.533304/s

    EXECUTION
    iteration_duration.............: avg=11.98s min=1s       med=2.57s    max=1m17s  p(90)=36.82s   p(95)=38.25s  
    iterations.....................: 3304   16.57889/s
    vus............................: 118    min=118         max=200
    vus_max........................: 200    min=200         max=200

    NETWORK
    data_received..................: 12 MB  59 kB/s
    data_sent......................: 4.1 MB 20 kB/s

