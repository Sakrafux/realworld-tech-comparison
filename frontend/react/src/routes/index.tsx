import { createFileRoute } from "@tanstack/react-router";
import { z } from "zod";
import HomeContent from "@/components/HomeContent.tsx";

const homeSearchSchema = z.object({
    feed: z.string().optional(),
    page: z.number().optional().catch(1),
});

export type HomeSearch = z.infer<typeof homeSearchSchema>;

export const Route = createFileRoute("/")({
    component: HomePage,
    validateSearch: homeSearchSchema,
});

function HomePage() {
    const search = Route.useSearch() as HomeSearch;
    return <HomeContent page={search.page ?? 1} feed={search.feed} />;
}
