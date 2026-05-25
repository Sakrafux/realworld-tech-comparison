import { createFileRoute, redirect } from "@tanstack/react-router";

export const Route = createFileRoute("/editor")({
    component: () => <div>Editor</div>,
    beforeLoad: ({ context }) => {
        if (!context.auth.isAuthenticated) {
            throw redirect({ to: "/login" });
        }
    },
});
