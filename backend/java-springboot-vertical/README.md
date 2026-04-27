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

![performance.png](performance.png)

### API test suite

- On startup: 3.00s
- After 10 warm-up runs: 1.34s
- After load test: 1.06s

### Load test suite

#### Light load (10 VUs / 30s)

    HTTP
    http_req_duration..............: avg=24.42ms min=1.29ms  med=8.12ms  max=457.31ms p(90)=73.46ms  p(95)=101.01ms
      { expected_response:true }...: avg=24.42ms min=1.29ms  med=8.12ms  max=457.31ms p(90)=73.46ms  p(95)=101.01ms
      { name:AddComment }..........: avg=11.12ms min=4.59ms  med=8.05ms  max=50.4ms   p(90)=20.1ms   p(95)=28.63ms 
      { name:CreateArticle }.......: avg=13.75ms min=5.28ms  med=9.89ms  max=91.36ms  p(90)=24.14ms  p(95)=36.78ms 
      { name:DeleteArticle }.......: avg=8.97ms  min=3.91ms  med=7.39ms  max=35.88ms  p(90)=15.66ms  p(95)=18.15ms 
      { name:DeleteComment }.......: avg=8.97ms  min=3.69ms  med=7.03ms  max=27.84ms  p(90)=16.59ms  p(95)=20.92ms 
      { name:FavoriteArticle }.....: avg=11.3ms  min=4.37ms  med=8.13ms  max=44.25ms  p(90)=21.04ms  p(95)=27.72ms 
      { name:FollowUser }..........: avg=7.91ms  min=3.31ms  med=5.97ms  max=27.81ms  p(90)=13.64ms  p(95)=17.74ms 
      { name:GetArticle }..........: avg=6.12ms  min=2.32ms  med=4.38ms  max=30.18ms  p(90)=11.42ms  p(95)=15.59ms 
      { name:GetArticlesFeed }.....: avg=4.92ms  min=1.8ms   med=3.53ms  max=22.66ms  p(90)=9.4ms    p(95)=12.5ms  
      { name:GetComments }.........: avg=6.62ms  min=2.39ms  med=4.71ms  max=44.17ms  p(90)=11.89ms  p(95)=17.1ms  
      { name:GetCurrentUser }......: avg=4.45ms  min=1.55ms  med=3.16ms  max=18.76ms  p(90)=7.98ms   p(95)=12.62ms 
      { name:GetGlobalArticles }...: avg=20.13ms min=8.56ms  med=15.71ms max=75.18ms  p(90)=36.64ms  p(95)=44.08ms 
      { name:GetTags }.............: avg=4.34ms  min=1.29ms  med=2.97ms  max=17.23ms  p(90)=8.98ms   p(95)=11.37ms 
      { name:Login }...............: avg=94.5ms  min=60.08ms med=76.17ms max=374ms    p(90)=153.3ms  p(95)=187.71ms
      { name:Register }............: avg=96.52ms min=62.93ms med=75.83ms max=457.31ms p(90)=151.22ms p(95)=189.38ms
      { name:UnfavoriteArticle }...: avg=11.46ms min=4.22ms  med=8.7ms   max=45.09ms  p(90)=21.46ms  p(95)=26.56ms 
      { name:UnfollowUser }........: avg=7.61ms  min=3.37ms  med=6.49ms  max=28.56ms  p(90)=13.08ms  p(95)=15.36ms 
    http_req_failed................: 0.00%  0 out of 3689
    http_reqs......................: 3689   118.022915/s

    EXECUTION
    iteration_duration.............: avg=1.41s   min=1.25s   med=1.36s   max=2.16s    p(90)=1.64s    p(95)=1.73s   
    iterations.....................: 217    6.942524/s
    vus............................: 2      min=2         max=10
    vus_max........................: 10     min=10        max=10

    NETWORK
    data_received..................: 2.9 MB 93 kB/s
    data_sent......................: 962 kB 31 kB/s

#### Medium load (50 VUs / 1m)

    HTTP
    http_req_duration..............: avg=172.84ms min=790.63µs med=123.95ms max=2.05s    p(90)=410.96ms p(95)=537.57ms
      { expected_response:true }...: avg=172.84ms min=790.63µs med=123.95ms max=2.05s    p(90)=410.96ms p(95)=537.57ms
      { name:AddComment }..........: avg=151.25ms min=3.02ms   med=118.55ms max=725.11ms p(90)=347.73ms p(95)=418.32ms
      { name:CreateArticle }.......: avg=191.86ms min=3.97ms   med=155.17ms max=1s       p(90)=426.41ms p(95)=535.71ms
      { name:DeleteArticle }.......: avg=108.38ms min=2.47ms   med=83.44ms  max=723.3ms  p(90)=252.58ms p(95)=319.97ms
      { name:DeleteComment }.......: avg=114.29ms min=2.47ms   med=87.06ms  max=1s       p(90)=270.48ms p(95)=336.96ms
      { name:FavoriteArticle }.....: avg=120.35ms min=2.8ms    med=93.11ms  max=874.19ms p(90)=269.62ms p(95)=333.63ms
      { name:FollowUser }..........: avg=95.31ms  min=2.24ms   med=72.67ms  max=740.28ms p(90)=225.56ms p(95)=278.37ms
      { name:GetArticle }..........: avg=131.24ms min=1.3ms    med=93.92ms  max=1.05s    p(90)=331.98ms p(95)=426.1ms 
      { name:GetArticlesFeed }.....: avg=109.14ms min=994.44µs med=85.21ms  max=849.81ms p(90)=266.34ms p(95)=340.84ms
      { name:GetComments }.........: avg=109.34ms min=1.3ms    med=74.83ms  max=1.08s    p(90)=263.99ms p(95)=368.28ms
      { name:GetCurrentUser }......: avg=181.51ms min=863.84µs med=127.44ms max=1.06s    p(90)=443.61ms p(95)=619.8ms 
      { name:GetGlobalArticles }...: avg=136.27ms min=7.15ms   med=115.16ms max=582.88ms p(90)=292.85ms p(95)=365.65ms
      { name:GetTags }.............: avg=94.72ms  min=790.63µs med=73.38ms  max=624.05ms p(90)=238.13ms p(95)=296.68ms
      { name:Login }...............: avg=397.52ms min=61.28ms  med=346.58ms max=1.92s    p(90)=719.39ms p(95)=893.28ms
      { name:Register }............: avg=393.08ms min=63.06ms  med=333.33ms max=2.05s    p(90)=656.63ms p(95)=841.44ms
      { name:UnfavoriteArticle }...: avg=110.9ms  min=3.12ms   med=89.85ms  max=969.25ms p(90)=248.05ms p(95)=315.3ms 
      { name:UnfollowUser }........: avg=100.09ms min=2.02ms   med=58.41ms  max=1.02s    p(90)=246.72ms p(95)=348.78ms
    http_req_failed................: 0.00%  0 out of 13328
    http_reqs......................: 13328  213.881685/s

    EXECUTION
    iteration_duration.............: avg=3.94s    min=1.25s    med=3.97s    max=5.89s    p(90)=4.73s    p(95)=5.03s   
    iterations.....................: 784    12.581276/s
    vus............................: 25     min=25         max=50
    vus_max........................: 50     min=50         max=50

    NETWORK
    data_received..................: 10 MB  165 kB/s
    data_sent......................: 3.5 MB 56 kB/s

#### Heavy load (200 VUs / 3m)

    HTTP
    http_req_duration..............: avg=1.46s  min=722.72µs med=703.25ms max=29.09s p(90)=1.8s   p(95)=2.67s 
      { expected_response:true }...: avg=1.46s  min=722.72µs med=703.25ms max=29.09s p(90)=1.8s   p(95)=2.67s 
      { name:AddComment }..........: avg=1.33s  min=2.72ms   med=681.02ms max=28.03s p(90)=1.73s  p(95)=2.37s 
      { name:CreateArticle }.......: avg=1.61s  min=3.25ms   med=751.82ms max=27.39s p(90)=1.98s  p(95)=2.98s 
      { name:DeleteArticle }.......: avg=1.21s  min=1.97ms   med=620.47ms max=27.82s p(90)=1.53s  p(95)=2.01s 
      { name:DeleteComment }.......: avg=1.3s   min=2.18ms   med=605.1ms  max=27.4s  p(90)=1.56s  p(95)=2.13s 
      { name:FavoriteArticle }.....: avg=1.33s  min=2.7ms    med=638.04ms max=28.09s p(90)=1.56s  p(95)=2.13s 
      { name:FollowUser }..........: avg=1.28s  min=1.37ms   med=650.35ms max=28.08s p(90)=1.71s  p(95)=2.3s  
      { name:GetArticle }..........: avg=1.42s  min=1.22ms   med=658.48ms max=28s    p(90)=1.6s   p(95)=2.34s 
      { name:GetArticlesFeed }.....: avg=1.28s  min=889.93µs med=674.36ms max=27.29s p(90)=1.75s  p(95)=2.34s 
      { name:GetComments }.........: avg=1.06s  min=1.32ms   med=598.09ms max=27.79s p(90)=1.52s  p(95)=1.93s 
      { name:GetCurrentUser }......: avg=1.49s  min=826.03µs med=740.79ms max=28.66s p(90)=1.97s  p(95)=2.83s 
      { name:GetGlobalArticles }...: avg=1.35s  min=10.04ms  med=698.71ms max=27.7s  p(90)=1.59s  p(95)=2.24s 
      { name:GetTags }.............: avg=1.27s  min=722.72µs med=637.94ms max=28.86s p(90)=1.58s  p(95)=2.13s 
      { name:Login }...............: avg=1.89s  min=66.21ms  med=941.63ms max=27.62s p(90)=2.65s  p(95)=4.37s 
      { name:Register }............: avg=2.37s  min=68.2ms   med=986.88ms max=29.09s p(90)=2.87s  p(95)=15.63s
      { name:UnfavoriteArticle }...: avg=1.15s  min=2.89ms   med=591.74ms max=28.26s p(90)=1.54s  p(95)=1.92s 
      { name:UnfollowUser }........: avg=1.19s  min=1.75ms   med=680.61ms max=27.83s p(90)=1.64s  p(95)=2.2s  
    http_req_failed................: 0.00%  0 out of 24616
    http_reqs......................: 24616  129.399591/s

    EXECUTION
    iteration_duration.............: avg=25.97s min=5.25s    med=19.25s   max=1m11s  p(90)=49.05s p(95)=1m2s  
    iterations.....................: 1448   7.611741/s
    vus............................: 25     min=25         max=200
    vus_max........................: 200    min=200        max=200

    NETWORK
    data_received..................: 19 MB  99 kB/s
    data_sent......................: 6.4 MB 34 kB/s
