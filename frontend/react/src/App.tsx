import {
    createRoute,
    createRouter,
    RouterProvider,
    Outlet,
    Navigate,
    redirect,
    createRootRouteWithContext,
} from "@tanstack/react-router";
import NavBar from "@/features/navigation/components/NavBar.tsx";
import Footer from "@/features/navigation/components/Footer.tsx";
import Home from "@/features/home/pages/Home.tsx";
import Login from "@/features/auth/pages/Login.tsx";
import Register from "@/features/auth/pages/Register.tsx";
import { type AuthContextType, useAuth } from "@/features/auth/context/auth-context.tsx";

const DummyComponent = () => <div />;

interface RouterContext {
    auth: AuthContextType;
}

// Root Route (matches every route -> navbar and similar)
const rootRoute = createRootRouteWithContext<RouterContext>()({
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
    component: Login,
});

const registerRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/register",
    component: Register,
});

// Settings
const settingsRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/settings",
    component: DummyComponent,
    beforeLoad: ({ context }) => {
        if (!context.auth.isAuthenticated) {
            throw redirect({ to: "/login" });
        }
    },
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
    context: {
        auth: undefined! as AuthContextType,
    },
});

export default function App() {
    const auth = useAuth();

    return <RouterProvider router={router} context={{ auth }} />;
}
