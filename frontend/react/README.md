# RealWorld Frontend: React ("Feature-Sliced")

This is an implementation of the [RealWorld UI](https://docs.realworld.show/specs/frontend-specs/introduction/) using React and a **Feature-Sliced Architecture**. It groups code by business domain rather than by technical role, keeping each feature self-contained while sharing a thin foundation of cross-cutting utilities.

## Architecture (Feature-Sliced)

The architecture is divided into three layers, organized by their role in the system:

- **Shared (`src/shared`)**: The foundation. Contains the HTTP client, API modules, shared types, and static assets. It has zero knowledge of page layout or component logic.
- **Features (`src/features`)**: The business features. Each subdirectory represents a self-contained feature module (article, auth, editor, home, navigation, profile, settings) with its own pages, components, and context providers.
    - *Independence*: Features rarely import from each other. When they do, it is through the shared layer — never directly.
    - *Thin Routes*: Route files delegate to page components inside features. They only handle route config, search param validation, and auth guards.
- **Routes (`src/routes`)**: The wiring layer. TanStack Router's file-based convention auto-generates the route tree. Route files are intentionally thin — they connect URLs to feature pages and enforce access control, but contain no UI logic.

## Tech Stack

- **React 19**
- **TanStack Router**: File-based routing with typed search params and code splitting
- **TanStack Query**: Server state management with optimistic cache updates
- **Zod**: Schema validation for route search parameters
- **Vite 8**: Build tooling with React Fast Refresh
- **react-markdown**: Article body rendering
- **TypeScript 6**: Strict mode with no unused locals/parameters

## Directory Structure

```text
.
├── public/
│   └── favicon.svg                  # App favicon
├── src/
│   ├── main.tsx                     # Entry point: AuthProvider > QueryClientProvider > Router
│   ├── App.tsx                      # Router creation with auth context
│   ├── index.css                    # Custom CSS framework (no external dependency)
│   ├── routes/                      # --- ROUTE DEFINITIONS (THIN WRAPPERS) ---
│   │   ├── __root.tsx               # Root layout: NavBar + Outlet + Footer
│   │   ├── index.tsx                # Home page route (Zod-validated search params)
│   │   ├── login.tsx                # Login route
│   │   ├── register.tsx             # Register route
│   │   ├── article.$slug.tsx        # Article detail route
│   │   ├── editor.tsx               # New article route (auth guard)
│   │   ├── editor.$slug.tsx         # Edit article route (auth guard)
│   │   ├── profile.$username.tsx    # User profile route (Zod search params)
│   │   └── settings.tsx             # Settings route (auth guard)
│   ├── features/                    # --- BUSINESS FEATURES (VERTICAL SLICES) ---
│   │   ├── article/                 # Article & Comment feature
│   │   │   ├── components/          #   ArticleMeta, ArticlePreview, Comments
│   │   │   └── pages/               #   ArticlePage
│   │   ├── auth/                    # Authentication feature
│   │   │   ├── context/             #   AuthProvider + useAuth hook
│   │   │   └── pages/               #   LoginPage, RegisterPage
│   │   ├── editor/                  # Article editor feature
│   │   │   └── pages/               #   EditorPage
│   │   ├── home/                    # Home feed feature
│   │   │   ├── components/          #   Tags
│   │   │   └── pages/               #   HomePage
│   │   ├── navigation/              # App chrome feature
│   │   │   └── components/          #   NavBar, Footer
│   │   ├── profile/                 # User profile feature
│   │   │   └── pages/               #   ProfilePage
│   │   └── settings/                # User settings feature
│   │       └── pages/               #   SettingsPage
│   └── shared/                      # --- CROSS-CUTTING UTILITIES ---
│       ├── api/
│       │   ├── api.ts               # Base HTTP client (auth token injection, 401 handling)
│       │   ├── events.ts            # Auth event bus (cross-tab logout signaling)
│       │   └── features/            # Domain API modules
│       │       ├── article-api.ts
│       │       ├── comment-api.ts
│       │       ├── profile-api.ts
│       │       ├── tag-api.ts
│       │       └── user-api.ts
│       ├── assets/                  # Static assets (default avatar)
│       └── types/                   # Shared TypeScript types (RouterContext)
├── index.html                       # HTML shell
├── vite.config.ts                   # Vite + TanStack Router plugin config
├── tsconfig.json                    # TypeScript project references
└── package.json
```

## Why this works for React

1. **Colocation**: Each feature groups its pages and components together, making it easy to find and modify related code without jumping across directories.
2. **No Global Store**: Auth state uses React Context; server state uses TanStack Query. There is no Zustand, Redux, or Jotai — the simplest tool for each concern.
3. **Optimistic by Default**: Mutations update the TanStack Query cache directly with `queryClient.setQueryData()`, giving instant UI feedback without full refetches.
4. **Typed Routing**: TanStack Router generates types for every route and validates search parameters with Zod, catching navigation bugs at compile time.
5. **Code Splitting**: The router plugin auto-splits each route into its own chunk, keeping the initial bundle small.

## Impressions

Feels great to use for any content during development, though this is likely biased by my experience.

TanStack query is a fantastic tool and provides a much better developer experience than Redux or simple React Context.
It allows to easily enforce reactive patterns and subsequent UI updates even across component boundaries, requiring neither cumbersome prop drilling
nor extensive glue code. It just works.

TanStack router is largely fine, but has its own idiosyncrasies one needs to deal with. While I am personally not a big fan of file-based routing, the necessary wiring
for code-based routing feels cumbersome and boilerplate-y. React Router's routes-as-elements system feels better in that regard.

The only issue with using React is that the resulting website's stats are not ideal for largely static content. In that case, i.e., websites, static site generation 
should be preferred. However, for interactive web applications it provides everything one needs with a large ecosystem. The production of a static bundle allows cheap
and easy hosting, e.g., CDNs.

## Testing

This implementation fully complies with the e2e test suite (`/test/e2e/`) and serves as a reference implementation.

## Performance

Lighthouse report available at [lighthouse.html](./lighthouse.html) with following scores:
- Performance = 73
- Accessibility = 93
- Best Practices = 100
- SEO = 83

Home page network stats:
- 20 requests
- 606 kB transferred
- 755 kB resources
- Finish: 245 ms
- DOMContentLoaded: 90 ms
- Load: 148 ms