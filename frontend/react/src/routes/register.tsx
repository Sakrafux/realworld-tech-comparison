import { createFileRoute } from "@tanstack/react-router";
import Register from "@/features/auth/pages/Register.tsx";

export const Route = createFileRoute("/register")({
    component: Register,
});
