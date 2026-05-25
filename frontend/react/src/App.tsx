import { createRouter, RouterProvider, Navigate } from "@tanstack/react-router";
import { type AuthContextType, useAuth } from "@/features/auth/context/auth-context.tsx";
import { routeTree } from "@/routeTree.gen.ts";

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
