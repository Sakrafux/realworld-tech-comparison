import { createFileRoute } from "@tanstack/react-router";
import LoginPage from "@/components/LoginPage.tsx";

export const Route = createFileRoute("/login")({
    component: LoginPage,
});
