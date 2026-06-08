import { createRouter, RouterProvider, Navigate } from "@tanstack/react-router";
import { routeTree } from "@/routeTree.gen.ts";
import { type AuthContextType, useAuth } from "@/components/auth-context.tsx";

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
