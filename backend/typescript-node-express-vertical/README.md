# RealWorld Backend: TypeScript + Express (Vertical Slice Architecture)

This is a conceptual layout for a **Vertical Slice Architecture** implementation using TypeScript and Express.

## Architectural Idea
This implementation focuses on **High Cohesion** by grouping code according to business features rather than technical layers. Every "slice" (folder) contains everything needed to fulfill a specific business requirement, from the API route down to the database query.

- **Feature-Centric**: Folders like `users/` or `articles/` contain the controller, service, repository, and validation logic for that specific domain.
- **Minimal Boilerplate**: Unlike Hexagonal Architecture, Vertical Slices in TypeScript often share models across the slice to maximize development speed while maintaining type safety.
- **Encapsulation**: Each feature manages its own dependencies. Global state is minimized, and cross-feature communication is handled through defined service interfaces.

## Potential Directory Structure

```text
src/
├── features/
│   ├── users/                   # --- USER SLICE ---
│   │   ├── user.controller.ts   # Express request/response handling
│   │   ├── user.service.ts      # Business logic & orchestration
│   │   ├── user.repository.ts   # Database interaction (e.g., Prisma, TypeORM)
│   │   ├── user.validator.ts    # Zod/Joi schemas for request validation
│   │   └── user.types.ts        # TypeScript interfaces for this feature
│   │
│   ├── articles/                # --- ARTICLE SLICE ---
│   │   ├── article.controller.ts
│   │   ├── article.service.ts
│   │   ├── article.repository.ts
│   │   └── ...
│   │
│   └── tags/
│       └── ...
│
├── core/                        # --- SHARED INFRASTRUCTURE ---
│   ├── middleware/              # Global auth, error handling, logging
│   ├── database/                # DB connection setup
│   ├── config/                  # Environment variables and global constants
│   └── errors/                  # Standardized error classes
│
├── app.ts                       # Express application setup
└── server.ts                    # Entry point
```

## Why this works for TypeScript/Express
1. **Type Safety without Overhead**: You can share TypeScript interfaces within a slice without needing complex mappers, but you still keep the features isolated.
2. **Easy Navigation**: When fixing a bug in "Comments," you only ever need to look inside the `features/comments/` folder.
3. **Scalability**: New features can be added by simply creating a new folder in `features/`, with zero risk of breaking existing functionality.
4. **Validation as a "Port"**: Using tools like **Zod** at the entry of the controller acts as a strict contract, ensuring only valid data enters the business logic.
