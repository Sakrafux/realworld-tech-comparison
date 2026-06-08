import { createFileRoute, redirect } from "@tanstack/react-router";
import SettingsPage from "@/components/SettingsPage.tsx";

export const Route = createFileRoute("/settings")({
    component: SettingsPage,
    beforeLoad: ({ context }) => {
        if (!context.auth.isAuthenticated) {
            throw redirect({ to: "/login" });
        }
    },
});
