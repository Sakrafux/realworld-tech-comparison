import api from "@/shared/api";

export type Profile = {
    username: string;
    bio: string;
    image: string;
    following: boolean;
};

export type ProfileResponse = {
    profile: Profile;
};

export async function getProfile(username: string): Promise<ProfileResponse> {
    return api.get<ProfileResponse>(`/api/profiles/${username}`);
}
