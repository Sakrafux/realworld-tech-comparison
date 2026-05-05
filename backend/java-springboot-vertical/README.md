# RealWorld Backend: Java Spring Boot (Vertical Slice Architecture)

This is an implementation of the [RealWorld API](https://docs.realworld.show/specs/backend-specs/introduction/) using Java and Spring Boot.

## Architecture

This implementation follows a **Vertical Slice Architecture** (Package-by-Feature):

- **Feature-Centric**: Each package (e.g., `article`, `user`, `comment`) contains all the components required for that specific feature, including Controllers, Services, Repositories, Entities, and DTOs.
- **High Cohesion**: Business logic, data access, and API definitions for a single domain are kept together, making features easier to find and modify in isolation.
- **Internal Simplicity**: Because the slices are extremely granular (one slice per domain concept), there is **no internal sub-layering**. Controllers, services, and repositories live side-by-side in the same package for maximum visibility and minimum boilerplate.
- **Cross-Cutting Concerns**: Infrastructure and shared utilities (Security, Global Exceptions, Configurations, Base Entities) reside in specialized `core` or `security` packages.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Data JPA**
- **H2 / PostgreSQL** (depending on configuration)
- **Maven**
- **Monitoring**: Spring Boot Actuator with Micrometer & Prometheus integration
- **Security**: Stateless JWT Authentication

## Directory Structure (Current: Granular Slices)

In this approach, slices are kept as small as possible to minimize complexity within a single package.

```text
src/
└── main/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── article/      # All article-related code (no sub-packages)
    │       ├── comment/      # All comment-related code
    │       ├── profile/      # All profile-related code
    │       ├── tag/          # All tag-related code
    │       ├── user/         # All user-related code
    │       ├── core/         # Shared infrastructure
    │       └── security/     # Auth logic
    └── resources/            # application.yml
```

## Alternative Approach: Feature-Based Layering

For larger projects, vertical slices can be grouped into broader business domains. In this scenario, each domain slice would internally follow a **Layered Architecture** to maintain order as the number of classes grows.

```text
src/
└── main/
    ├── java/
    │   └── com.sakrafux.realworld/
    │       ├── article/              # --- ARTICLE DOMAIN ---
    │       │   ├── controller/       # Controllers & DTOs for Article, Comment, Tag
    │       │   ├── service/          # Business logic
    │       │   ├── repository/       # Repositories
    │       │   └── entity/           # JPA Entities
    │       │
    │       ├── user/                 # --- USER DOMAIN ---
    │       │   ├── controller/       # Controllers & DTOs for User & Profile
    │       │   ├── service/          
    │       │   ├── repository/       
    │       │   └── entity/           
    │       │
    │       ├── core/                 # Shared logic
    │       └── security/             
    └── resources/            
```

## Testing

This project prioritizes the global **API Test Suite** (located in the root `/test/api` directory) as the primary verification tool for specification compliance. 

Individual Unit and Integration tests within this module are largely derivative of the logic found in the [Layered Architecture](../java-springboot-layered) implementation. For this reason, and due to the heavy reliance on the end-to-end API suite, additional module-specific tests are kept minimal to avoid redundancy while ensuring business rules are verified within their respective feature packages.

## Performance

- Max CPU Utilization: 6.39%
- Max Memory Usage: 496 MiB
- Max Requests per Second: 232 / 197

![performance.png](performance.png)

### API test suite

- On startup: 3.3s
- After 10 warm-up runs: 1.38s
- After load test: 1.06s

### Load test suite

#### Light load (10 VUs / 30s)

    HTTP
    http_req_duration..............: avg=8.89ms  min=1.41ms  med=5.9ms   max=233.01ms p(90)=12.5ms  p(95)=19.17ms 
      { expected_response:true }...: avg=8.89ms  min=1.41ms  med=5.9ms   max=233.01ms p(90)=12.46ms p(95)=19.18ms 
      { name:AddComment }..........: avg=8.88ms  min=5.02ms  med=7.02ms  max=46.56ms  p(90)=12.13ms p(95)=15.75ms 
      { name:CreateArticle }.......: avg=12.16ms min=6.65ms  med=8.88ms  max=149.53ms p(90)=13.83ms p(95)=16.92ms 
      { name:DeleteArticle }.......: avg=7.13ms  min=4.07ms  med=5.64ms  max=25.13ms  p(90)=11.81ms p(95)=14.11ms 
      { name:DeleteComment }.......: avg=6.64ms  min=4.21ms  med=5.51ms  max=24.61ms  p(90)=10.46ms p(95)=11.94ms 
      { name:FavoriteArticle }.....: avg=8.5ms   min=5.27ms  med=6.94ms  max=33.49ms  p(90)=12.57ms p(95)=16.68ms 
      { name:FollowUser }..........: avg=6.41ms  min=3.68ms  med=5.39ms  max=30.85ms  p(90)=8.62ms  p(95)=11.07ms 
      { name:GetArticle }..........: avg=4.5ms   min=2.24ms  med=3.8ms   max=29.16ms  p(90)=6.05ms  p(95)=7.71ms  
      { name:GetArticlesFeed }.....: avg=3.8ms   min=2.22ms  med=3.07ms  max=22.17ms  p(90)=5.53ms  p(95)=7.89ms  
      { name:GetComments }.........: avg=5.12ms  min=2.74ms  med=3.95ms  max=25.72ms  p(90)=7.87ms  p(95)=10.33ms 
      { name:GetCurrentUser }......: avg=3.71ms  min=2.16ms  med=2.62ms  max=11.83ms  p(90)=6.26ms  p(95)=9.16ms  
      { name:GetGlobalArticles }...: avg=12.61ms min=7.78ms  med=9.76ms  max=83.54ms  p(90)=18.33ms p(95)=23.01ms 
      { name:GetProfile }..........: avg=3.84ms  min=2ms     med=3.26ms  max=11.12ms  p(90)=5.97ms  p(95)=7.55ms  
      { name:GetTags }.............: avg=3.05ms  min=1.41ms  med=2.38ms  max=23.21ms  p(90)=5.01ms  p(95)=5.75ms  
      { name:Login }...............: avg=70.1ms  min=62.57ms med=64.35ms max=167.89ms p(90)=76.32ms p(95)=82.88ms 
      { name:Register }............: avg=76.18ms min=65ms    med=67.45ms max=233.01ms p(90)=87.99ms p(95)=100.65ms
      { name:UnfavoriteArticle }...: avg=8.43ms  min=5.26ms  med=7.07ms  max=24.46ms  p(90)=13.23ms p(95)=14.93ms 
      { name:UnfollowUser }........: avg=6.01ms  min=3.67ms  med=5.23ms  max=17.32ms  p(90)=8.75ms  p(95)=11.23ms 
    http_req_failed................: 0.08%  2 out of 2345
    http_reqs......................: 2345   73.927401/s

    EXECUTION
    iteration_duration.............: avg=1.07s   min=1s      med=1.06s   max=1.35s    p(90)=1.1s    p(95)=1.14s   
    iterations.....................: 285    8.98478/s
    vus............................: 8      min=8         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 1.9 MB 58 kB/s
    data_sent......................: 640 kB 20 kB/s

#### Medium load (50 VUs / 1m)

    HTTP
    http_req_duration..............: avg=94.55ms  min=992.65µs med=7.96ms  max=17.87s   p(90)=71.07ms  p(95)=115.55ms
      { expected_response:true }...: avg=94.57ms  min=992.65µs med=7.95ms  max=17.87s   p(90)=71.03ms  p(95)=115.08ms
      { name:AddComment }..........: avg=26.53ms  min=3.7ms    med=9.34ms  max=289.37ms p(90)=70.1ms   p(95)=112.39ms
      { name:CreateArticle }.......: avg=434.45ms min=4.76ms   med=11.94ms max=17.63s   p(90)=86.62ms  p(95)=213.25ms
      { name:DeleteArticle }.......: avg=22.55ms  min=2.73ms   med=7.89ms  max=674.02ms p(90)=51.47ms  p(95)=83.13ms 
      { name:DeleteComment }.......: avg=23.1ms   min=2.45ms   med=7.79ms  max=625.21ms p(90)=50.33ms  p(95)=75.04ms 
      { name:FavoriteArticle }.....: avg=26.55ms  min=3.61ms   med=9.03ms  max=291.44ms p(90)=66.61ms  p(95)=109.59ms
      { name:FollowUser }..........: avg=18.55ms  min=2.35ms   med=7.05ms  max=303.99ms p(90)=44.71ms  p(95)=71.89ms 
      { name:GetArticle }..........: avg=18.37ms  min=1.66ms   med=5.51ms  max=379.77ms p(90)=42.8ms   p(95)=70.31ms 
      { name:GetArticlesFeed }.....: avg=13.8ms   min=1.05ms   med=4.33ms  max=267.2ms  p(90)=37.55ms  p(95)=57.74ms 
      { name:GetComments }.........: avg=17.61ms  min=1.72ms   med=5.12ms  max=273.94ms p(90)=44.36ms  p(95)=78.93ms 
      { name:GetCurrentUser }......: avg=17.85ms  min=1.29ms   med=3.83ms  max=388.57ms p(90)=48.5ms   p(95)=81.25ms 
      { name:GetGlobalArticles }...: avg=33.23ms  min=3.9ms    med=10.98ms max=689.38ms p(90)=80.44ms  p(95)=120.43ms
      { name:GetProfile }..........: avg=217.77ms min=1.05ms   med=4.6ms   max=17.57s   p(90)=41.43ms  p(95)=72.32ms 
      { name:GetTags }.............: avg=13.58ms  min=992.65µs med=3.12ms  max=348.89ms p(90)=33.92ms  p(95)=63.27ms 
      { name:Login }...............: avg=535.01ms min=61.82ms  med=83.31ms max=17.87s   p(90)=325.71ms p(95)=412.19ms
      { name:Register }............: avg=516.16ms min=63.64ms  med=84.35ms max=17.68s   p(90)=329.55ms p(95)=529.81ms
      { name:UnfavoriteArticle }...: avg=63.69ms  min=3.39ms   med=9.45ms  max=17.63s   p(90)=71.43ms  p(95)=110.08ms
      { name:UnfollowUser }........: avg=20.76ms  min=2.32ms   med=7.06ms  max=398.41ms p(90)=41.29ms  p(95)=75.76ms 
    http_req_failed................: 0.09%  12 out of 13096
    http_reqs......................: 13096  189.381266/s

    EXECUTION
    iteration_duration.............: avg=1.58s    min=1s       med=1.07s   max=19.18s   p(90)=1.47s    p(95)=1.84s   
    iterations.....................: 2129   30.787471/s
    vus............................: 33     min=0           max=50
    vus_max........................: 50     min=50          max=50

    NETWORK
    data_received..................: 10 MB  146 kB/s
    data_sent......................: 3.5 MB 51 kB/s

#### Heavy load (200 VUs / 3m)

    HTTP
    http_req_duration..............: avg=2.57s  min=806.74µs med=305.42ms max=34.41s p(90)=1.04s    p(95)=31.39s  
      { expected_response:true }...: avg=2.53s  min=806.74µs med=304.96ms max=34.25s p(90)=1.02s    p(95)=31.32s  
      { name:AddComment }..........: avg=1.36s  min=2.62ms   med=303.53ms max=33.78s p(90)=785.61ms p(95)=1.21s   
      { name:CreateArticle }.......: avg=6.53s  min=4.28ms   med=386.53ms max=33.86s p(90)=31.52s   p(95)=32.09s  
      { name:DeleteArticle }.......: avg=1.23s  min=2.01ms   med=254.22ms max=33.7s  p(90)=687.88ms p(95)=965.51ms
      { name:DeleteComment }.......: avg=1.63s  min=2.2ms    med=288.71ms max=33.77s p(90)=792.84ms p(95)=1.42s   
      { name:FavoriteArticle }.....: avg=1.35s  min=2.93ms   med=286.86ms max=34s    p(90)=718.96ms p(95)=953.67ms
      { name:FollowUser }..........: avg=1.29s  min=1.3ms    med=287.95ms max=33.95s p(90)=728.66ms p(95)=1.05s   
      { name:GetArticle }..........: avg=1.33s  min=1.22ms   med=290.59ms max=33.83s p(90)=800.99ms p(95)=1.06s   
      { name:GetArticlesFeed }.....: avg=1.54s  min=1.05ms   med=271.2ms  max=34.28s p(90)=738.38ms p(95)=1.21s   
      { name:GetComments }.........: avg=1.26s  min=1.24ms   med=275.82ms max=33.78s p(90)=706.9ms  p(95)=926.6ms 
      { name:GetCurrentUser }......: avg=1.25s  min=870.94µs med=263.67ms max=34.06s p(90)=736.95ms p(95)=968.91ms
      { name:GetGlobalArticles }...: avg=1.57s  min=7.39ms   med=289.26ms max=34.25s p(90)=745.1ms  p(95)=1.17s   
      { name:GetProfile }..........: avg=5.86s  min=1.14ms   med=345.61ms max=34.23s p(90)=31.43s   p(95)=32.02s  
      { name:GetTags }.............: avg=1.34s  min=806.74µs med=240.34ms max=33.58s p(90)=688.17ms p(95)=1.08s   
      { name:Login }...............: avg=7.14s  min=66.89ms  med=614.72ms max=34.41s p(90)=31.63s   p(95)=32.1s   
      { name:Register }............: avg=6.76s  min=64.16ms  med=588.54ms max=34.14s p(90)=31.79s   p(95)=32.34s  
      { name:UnfavoriteArticle }...: avg=1.3s   min=3.1ms    med=271.57ms max=34.04s p(90)=724.81ms p(95)=1.09s   
      { name:UnfollowUser }........: avg=1.31s  min=1.94ms   med=287.51ms max=33.95s p(90)=818.69ms p(95)=1.11s   
    http_req_failed................: 0.24%  35 out of 14111
    http_reqs......................: 14111  71.114881/s

    EXECUTION
    iteration_duration.............: avg=12.54s min=1s       med=2.94s    max=1m9s   p(90)=34.81s   p(95)=35.7s   
    iterations.....................: 3144   15.844744/s
    vus............................: 121    min=121         max=200
    vus_max........................: 200    min=200         max=200

    NETWORK
    data_received..................: 11 MB  56 kB/s
    data_sent......................: 3.8 MB 19 kB/s

