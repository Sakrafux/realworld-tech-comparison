import api from "@/shared/api";

export type ProfileResponse = {
    profile: {
        username: string;
        bio: string;
        image: string;
        following: boolean;
    };
};

export async function getProfile(username: string): Promise<ProfileResponse> {
    return api.get<ProfileResponse>(`/api/profiles/${username}`);
}
