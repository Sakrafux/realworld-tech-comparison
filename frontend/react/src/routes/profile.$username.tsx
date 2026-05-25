import { createFileRoute } from "@tanstack/react-router";
import ProfilePage from "@/features/profile/pages/ProfilePage.tsx";
import { z } from "zod";

const profileSearchSchema = z.object({
    tab: z.string().optional(),
    page: z.number().optional().catch(1),
});

export type ProfileSearch = z.infer<typeof profileSearchSchema>;

export const Route = createFileRoute("/profile/$username")({
    component: ProfilePage,
    validateSearch: profileSearchSchema,
});
