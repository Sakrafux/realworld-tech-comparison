import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/profile/$username/favorites")({
    component: () => <div>Profile + Username + Favorites</div>,
});
