import { createFileRoute, redirect } from "@tanstack/react-router";
import EditorPage from "@/components/EditorPage.tsx";

export const Route = createFileRoute("/editor")({
    component: EditorPage,
    beforeLoad: ({ context }) => {
        if (!context.auth.isAuthenticated) {
            throw redirect({ to: "/login" });
        }
    },
});
