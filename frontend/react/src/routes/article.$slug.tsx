import { createFileRoute } from "@tanstack/react-router";
import ArticlePage from "@/components/ArticlePage.tsx";

export const Route = createFileRoute("/article/$slug")({
    component: ArticlePage,
});
