import api from "@/shared/api";

export type TagsResponse = {
    tags: string[];
};

export async function getTags(): Promise<TagsResponse> {
    return api.get<TagsResponse>("/tags");
}
