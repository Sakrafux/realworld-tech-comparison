# RealWorld Frontend: React ("Flat Components")

This is an implementation of the [RealWorld UI](https://docs.realworld.show/specs/frontend-specs/introduction/) using React with a **flat component architecture**. Components, pages, and auth context live in a single `src/components/` directory, with route files wiring them to URLs and a shared API layer handling all server communication.

## Architecture (Flat Components)

The architecture is divided into three areas, organized by their role in the system:

- **Components (`src/components`)**: All UI components in a flat directory — page components, navigation, auth context, and feature-specific pieces like article meta and comments. Each component is self-contained with no nesting.
- **Routes (`src/routes`)**: TanStack Router's file-based convention auto-generates the route tree. Route files are intentionally thin — they connect URLs to component pages, validate search params with Zod, and enforce auth guards, but contain no UI logic.
- **Shared (`src/shared`)**: The foundation. Contains the HTTP client, API modules, and shared types. It has zero knowledge of page layout or component logic.

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
│   ├── routeTree.gen.ts             # Auto-generated route tree (TanStack Router)
│   ├── components/                  # --- ALL UI COMPONENTS (FLAT) ---
│   │   ├── auth-context.tsx         # AuthProvider + useAuth hook
│   │   ├── NavBar.tsx               # Top navigation bar (auth-aware)
│   │   ├── Footer.tsx               # Site footer
│   │   ├── LoginPage.tsx            # Login page component
│   │   ├── RegisterPage.tsx         # Registration page component
│   │   ├── HomeContent.tsx          # Home feed with pagination
│   │   ├── Tags.tsx                 # Popular tags sidebar
│   │   ├── ArticlePage.tsx          # Article detail page component
│   │   ├── ArticleMeta.tsx          # Article metadata (author, date, favorites)
│   │   ├── ArticlePreview.tsx       # Article card for feed listings
│   │   ├── Comments.tsx             # Comment list with create/delete
│   │   ├── EditorPage.tsx           # Article editor form (create + edit)
│   │   ├── ProfilePage.tsx          # User profile page component
│   │   ├── SettingsPage.tsx         # User settings page component
│   ├── routes/                      # --- ROUTE DEFINITIONS (THIN WRAPPERS) ---
│   │   ├── __root.tsx               # Root layout: NavBar + Outlet + Footer
│   │   ├── index.tsx                # Home page route (Zod-validated search params)
│   │   ├── login.tsx                # Login route
│   │   ├── register.tsx             # Register route
│   │   ├── article.$slug.tsx        # Article detail route
│   │   ├── editor.tsx               # New article route (auth guard)
│   │   ├── editor.$slug.tsx         # Edit article route (auth guard)
│   │   ├── profile.$username.tsx    # User profile route
│   │   ├── profile.$username.favorites.tsx  # User favorites route
│   │   ├── settings.tsx             # Settings route (auth guard)
│   │   └── tag.$tag.tsx             # Tag-filtered articles route
│   └── shared/                      # --- CROSS-CUTTING UTILITIES ---
│       ├── api/
│       │   ├── api.ts               # Base HTTP client (auth token injection, 401 handling)
│       │   ├── events.ts            # Auth event bus (cross-tab logout signaling)
│       │   ├── index.ts             # API barrel export
│       │   └── features/            # Domain API modules
│       │       ├── article-api.ts
│       │       ├── comment-api.ts
│       │       ├── profile-api.ts
│       │       ├── tag-api.ts
│       │       └── user-api.ts
│       └── types/
│           └── router-types.ts      # Shared TypeScript types (RouterContext)
├── index.html                       # HTML shell
├── vite.config.ts                   # Vite + TanStack Router plugin config
├── tsconfig.json                    # TypeScript project references
└── package.json
```

## Why this works for React

1. **Discoverability**: All components in a single flat directory make it easy to find any piece of UI — no digging through nested feature folders or guessing which feature a shared component belongs to.
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