import { createFileRoute } from "@tanstack/react-router";
import ArticlePage from "@/features/article/pages/ArticlePage.tsx";

export const Route = createFileRoute("/article/$slug")({
    component: ArticlePage,
});
