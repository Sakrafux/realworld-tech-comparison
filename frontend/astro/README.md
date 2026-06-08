# RealWorld Frontend: Astro ("SSR + Islands")

This is an implementation of the [RealWorld UI](https://docs.realworld.show/specs/frontend-specs/introduction/) using Astro with a **hybrid SSR + Islands architecture**. Astro pages render the static HTML shell server-side, while Preact components hydrate as interactive "islands" only where needed — minimizing client-side JavaScript for content-heavy pages while preserving full interactivity for dynamic features.

## Architecture (SSR + Islands)

The architecture is organized into four concerns, each leveraging Astro's rendering model:

- **Pages (`src/pages`)**: File-system-based routing with `.astro` files that handle SSR data fetching and HTML structure. Dynamic routes like `[slug].astro` and `[...route].astro` use `prerender = false` for on-demand server rendering. Server API routes (`api/login.ts`, `api/logout.ts`) manage cookie-based authentication.
- **Layouts (`src/layouts`)**: A single `BaseLayout.astro` provides the common HTML shell — head, NavBar, slot content, and Footer — rendered entirely on the server.
- **Components (`src/components`)**: Interactive UI lives in Preact `.tsx` files hydrated via Astro's `client:load` or `client:only="react"` directives. Static, non-interactive components like Footer remain pure `.astro` files with zero client-side JS.
- **API + Auth (`src/api`, `src/util`)**: A dual auth strategy bridges client and server — Preact components use `localStorage` for JWT tokens, while SSR pages read `HttpOnly` cookies set by server API routes. The `api` object auto-injects auth headers client-side; `ssrGet()` accepts an explicit token for server-side fetches.

## Tech Stack

- **Astro 6**: File-system routing, SSR with Node adapter, View Transitions
- **Preact 10**: Interactive component islands hydrated on the client
- **marked**: Server-side markdown rendering for article bodies
- **TypeScript**: Strict mode via `astro/tsconfigs/strict`
- **Vite**: Build tooling with Astro integration
- **Node adapter**: Standalone SSR mode for on-demand dynamic routes
- **Custom CSS**: Handcrafted minimal subset of Bootstrap classes (~1500 lines, no framework dependency)

## Directory Structure

```text
.
├── public/
│   ├── default-avatar.svg           # Default user avatar
│   ├── favicon.svg                  # App favicon
│   └── index.css                    # Handcrafted CSS (minimal Bootstrap subset)
├── src/
│   ├── api/
│   │   ├── api.ts                   # HTTP client (localStorage auth, 401 handling, ssrGet)
│   │   └── features/               # Domain API modules
│   │       ├── article-api.ts
│   │       ├── comment-api.ts
│   │       ├── profile-api.ts
│   │       ├── tag-api.ts
│   │       └── user-api.ts
│   ├── components/
│   │   ├── ArticleMeta.tsx          # Preact: Article metadata (author, date, favorites)
│   │   ├── ArticlePreview.tsx       # Preact: Article card for feed listings
│   │   ├── AuthGuard.tsx            # Preact: Auth guard wrapper (redirects to /login)
│   │   ├── Comments.tsx             # Preact: Comment list with create/delete
│   │   ├── EditorPage.tsx           # Preact: Article editor form
│   │   ├── Footer.astro             # Astro: Static footer (zero JS)
│   │   ├── HomeArticles.tsx         # Preact: Feed/article listing with pagination
│   │   ├── LoginPage.tsx            # Preact: Login form
│   │   ├── NavBar.tsx                # Preact: Navigation bar (auth-aware)
│   │   ├── ProfileActions.tsx        # Preact: Follow/unfollow button
│   │   ├── ProfileArticles.tsx       # Preact: User's articles listing
│   │   ├── RegisterPage.tsx          # Preact: Registration form
│   │   ├── SettingsPage.tsx           # Preact: User settings form
│   │   └── Tags.tsx                  # Preact: Popular tags sidebar
│   ├── layouts/
│   │   └── BaseLayout.astro          # Root layout: head, NavBar, slot, Footer
│   ├── pages/
│   │   ├── api/
│   │   │   ├── login.ts              # Server API route: set auth cookie
│   │   │   └── logout.ts             # Server API route: clear auth cookie
│   │   ├── article/
│   │   │   └── [slug].astro          # Article detail (SSR data fetch + Preact islands)
│   │   ├── editor/
│   │   │   └── [...route].astro      # Article editor (new + edit)
│   │   ├── profile/
│   │   │   └── [...route].astro      # User profile (articles + favorites)
│   │   ├── tag/
│   │   │   └── [tag].astro           # Tag-filtered articles
│   │   ├── index.astro               # Home page
│   │   ├── login.astro               # Login page
│   │   ├── register.astro             # Register page
│   │   └── settings.astro             # Settings page (AuthGuard wrapper)
│   └── util/
│       ├── active-path-util.ts       # Path comparison for nav highlighting
│       └── auth-util.ts              # Auth helpers: login, logout, isAuthenticated
├── astro.config.mjs                  # Astro + Preact + Node adapter config
├── Dockerfile                        # Multi-stage Docker build
├── tsconfig.json                     # Strict TS, Preact JSX, @/* path alias
└── package.json
```

## Why this works for Astro

1. **Minimal Client JS**: Static components like Footer stay as `.astro` files with zero JavaScript shipped. Only interactive parts — forms, like/follow toggles, pagination — hydrate as Preact islands.
2. **Server-Rendered Content**: Article bodies are fetched and rendered server-side via `ssrGet()` and `marked.parse()`, delivering fully formed HTML to the browser with no client-side data fetching for the initial view.
3. **Dual Auth Bridge**: The `localStorage` + `HttpOnly` cookie strategy means Preact islands authenticate via headers while SSR pages authenticate via cookies — both work seamlessly without conflicting.
4. **Selective Hydration**: `client:load` for immediately-needed interactivity (NavBar, forms) and `client:only` for components that depend entirely on client state (EditorPage, SettingsPage), letting each island choose its hydration strategy.
5. **Handcrafted CSS**: No framework dependency — `public/index.css` contains only the Bootstrap classes actually used by the project, keeping the stylesheet minimal and maintainable.

## Impressions

Feels great to use for static content with some interactivity. 
It's easy to build the website in a modular manner while adding some minor interactivity using the island system.

However, greater interactivity, i.e., forms, feel very wrong as they are wholly interactive, meaning the whole page
is basically one large island. While this is fine for sparse usage, if it occurs for a large part of the application
one may wonder, why even use Astro in the first place.

Statically generated dynamic routes feel good to use. On the other hand, on-demand dynamic routes feel very wonky 
to implement. Mainly because this is the only real use case forcing SSR, which then implies some further consequences.
Not only does it require handling authentication also via cookie for the use in SSR, other features like caching
must now be controlled via headers. And most obvious, deployment now requires a server environment instead of static
bundle to be served from anywhere.

I would use it for any kind of website that relies on static content where interactivity is largely contained on the
client and is mainly used in order to enhance UX. Though some sporadic form-like elements are largely fine as well.

Even some applications that theoretically rely on on-demand dynamic routes may be fine depending on the nature of the
demand. E.g., a personal blog that allows one to add more blog pages. The specific blog pages would be dynamic routes,
but due to long-lived nature of a blog, we could re-generate the static bundle on publish and re-deploy it.

## Testing

This implementation doesn't comply completely with the e2e test suite (`/test/e2e`), since it only serves as POC of the
technology, and fixing all details is not necessary.

## Performance

Lighthouse report available at [lighthouse.html](./lighthouse.html) with following scores:
- Performance = 76
- Accessibility = 90
- Best Practices = 100
- SEO = 91

Home page network stats:
- 29 requests
- 284 kB transferred
- 429 kB resources
- Finish: 943 ms
- DOMContentLoaded: 647 ms
- Load: 928 ms