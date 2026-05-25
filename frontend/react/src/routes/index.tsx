import { createFileRoute } from "@tanstack/react-router";
import Home from "@/features/home/pages/Home.tsx";
import { z } from "zod";

const homeSearchSchema = z.object({
    personal: z.boolean().optional().catch(false),
    tag: z.string().optional(),
    page: z.number().optional().catch(1),
});

export type HomeSearch = z.infer<typeof homeSearchSchema>;

export const Route = createFileRoute("/")({
    component: Home,
    validateSearch: homeSearchSchema,
});
