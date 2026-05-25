import { createFileRoute } from "@tanstack/react-router";
import Login from "@/features/auth/pages/Login.tsx";

export const Route = createFileRoute("/login")({
    component: Login,
});
