import { createFileRoute } from "@tanstack/react-router";
import HomePage from "@/features/home/pages/HomePage.tsx";
import { z } from "zod";

const homeSearchSchema = z.object({
    feed: z.string().optional(),
    tag: z.string().optional(),
    page: z.number().optional().catch(1),
});

export type HomeSearch = z.infer<typeof homeSearchSchema>;

export const Route = createFileRoute("/")({
    component: HomePage,
    validateSearch: homeSearchSchema,
});
