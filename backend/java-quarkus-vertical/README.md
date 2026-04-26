# RealWorld Backend: Java Quarkus (Vertical Slice Architecture)

This is a conceptual layout for a **Vertical Slice Architecture** implementation using Quarkus and Hibernate Panache.

## Architectural Idea
This implementation prioritizes developer productivity and runtime performance by grouping code into business features and utilizing the **Active Record Pattern** via Panache.

- **High Cohesion**: Each package (e.g., `article`, `user`) contains the REST Resource, Service logic, and the Panache Entity.
- **Active Record**: Business entities extend `PanacheEntity`, combining data and persistence logic for maximum conciseness.
- **Developer Joy**: Quarkus' live-reload capabilities work best when the directory structure is flat and intuitive.

## Potential Directory Structure

```text
.
├── src/main/java/com/sakrafux/realworld/
│   ├── article/            # --- ARTICLE SLICE ---
│   │   ├── ArticleResource.java  # REST API
│   │   ├── ArticleService.java   # Complex business logic
│   │   ├── Article.java          # Panache Entity (Active Record)
│   │   └── ArticleMapper.java    # Mapping to DTOs
│   │
│   ├── user/               # --- USER SLICE ---
│   │   └── ...
│   │
│   └── core/               # Shared security (OIDC/JWT), Filters, and Config
├── pom.xml
└── README.md
```

## Why this works for Quarkus
Quarkus is optimized for "Direct Data Access." By using Vertical Slices with Panache, you write significantly less code while maintaining high performance. The isolation between slices ensures that the "Active Record" coupling doesn't lead to a "Big Ball of Mud."
