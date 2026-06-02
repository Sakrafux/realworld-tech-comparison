import { createFileRoute } from "@tanstack/react-router";
import { z } from "zod";
import HomeContent from "@/features/home/pages/HomeContent.tsx";

const tagSearchSchema = z.object({
    page: z.number().optional().catch(1),
});

export const Route = createFileRoute("/tag/$tag")({
    component: TagPage,
    validateSearch: tagSearchSchema,
});

function TagPage() {
    const { tag } = Route.useParams();
    const search = Route.useSearch();
    return <HomeContent tag={tag} page={search.page ?? 1} />;
}
