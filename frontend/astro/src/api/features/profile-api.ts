import api from "@/api/api";

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
    return api.get<ProfileResponse>(`/profiles/${username}`);
}

export async function followUserByUsername(username: string): Promise<ProfileResponse> {
    return api.post<ProfileResponse>(`/profiles/${username}/follow`);
}

export async function unfollowUserByUsername(username: string): Promise<ProfileResponse> {
    return api.delete<ProfileResponse>(`/profiles/${username}/follow`);
}
