import api from "@/api/api.ts";

export type TagsResponse = {
    tags: string[];
};

export async function getTags(): Promise<TagsResponse> {
    return api.get<TagsResponse>("/tags");
}
