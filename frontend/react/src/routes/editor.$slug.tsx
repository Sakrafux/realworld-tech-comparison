import { createFileRoute, redirect } from "@tanstack/react-router";

export const Route = createFileRoute("/editor/$slug")({
    component: () => <div>Editor + Slug</div>,
    beforeLoad: ({ context }) => {
        if (!context.auth.isAuthenticated) {
            throw redirect({ to: "/login" });
        }
    },
});
