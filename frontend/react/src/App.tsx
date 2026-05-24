import {
    createRootRoute,
    createRoute,
    createRouter,
    RouterProvider,
    Outlet,
    Navigate,
} from "@tanstack/react-router";
import NavBar from "@/features/navigation/components/NavBar.tsx";
import Footer from "@/features/navigation/components/Footer.tsx";
import Home from "@/features/home/pages/Home.tsx";

const DummyComponent = () => <div />;

// Root Route (matches every route -> navbar and similar)
const rootRoute = createRootRoute({
    component: () => (
        <div>
            <NavBar />
            <Outlet />
            <Footer />
        </div>
    ),
});

// Home
const indexRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/",
    component: Home,
});

// Auth
const loginRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/login",
    component: DummyComponent,
});

const registerRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/register",
    component: DummyComponent,
});

// Settings
const settingsRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/settings",
    component: DummyComponent,
});

// Article
const articleRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/article/$slug",
    component: DummyComponent,
});

// Editor
const editorIndexRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/editor",
    component: DummyComponent,
});

const editorSlugRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/editor/$slug",
    component: DummyComponent,
});

// Profile
const profileRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/profile/$username",
    component: DummyComponent,
});

const profileFavoritesRoute = createRoute({
    getParentRoute: () => profileRoute,
    path: "favorites",
    component: DummyComponent,
});

const routeTree = rootRoute.addChildren([
    indexRoute,
    loginRoute,
    registerRoute,
    settingsRoute,
    editorIndexRoute,
    editorSlugRoute,
    articleRoute,
    profileRoute.addChildren([profileFavoritesRoute]),
]);

const router = createRouter({
    routeTree,
    defaultNotFoundComponent: () => {
        return <Navigate to="/" replace />;
    },
});

export default function App() {
    return <RouterProvider router={router} />;
}
