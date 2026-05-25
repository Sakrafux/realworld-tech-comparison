import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/profile/$username")({
    component: () => <div>Profile + Username</div>,
});
