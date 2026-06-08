import { createFileRoute } from "@tanstack/react-router";
import { z } from "zod";
import ProfilePage from "@/components/ProfilePage.tsx";

const profileSearchSchema = z.object({
    page: z.number().optional().catch(1),
});

export type ProfileFavoritesSearch = z.infer<typeof profileSearchSchema>;

export const Route = createFileRoute("/profile/$username/favorites")({
    component: ProfilePage,
    validateSearch: profileSearchSchema,
});
