import type { Profile } from "@/shared/api/features/profile-api.ts";
import api from "@/shared/api";

export type NewComment = {
    body: string;
};

export type Comment = {
    id: number;
    body: string;
    createdAt: string; // date-time
    updatedAt: string; // date-time
    author: Profile;
};

export type SingleCommentResponse = {
    comment: Comment;
};

export type MultipleCommentsResponse = {
    comments: Comment[];
};

export async function getArticleComments(slug: string): Promise<MultipleCommentsResponse> {
    return api.get<MultipleCommentsResponse>(`/articles/${slug}/comments`);
}

export async function createArticleComment(
    slug: string,
    newComment: NewComment,
): Promise<SingleCommentResponse> {
    return api.post<SingleCommentResponse>(`/articles/${slug}/comments`, { comment: newComment });
}

export async function deleteArticleComment(slug: string, id: number) {
    return api.delete(`/articles/${slug}/comments/${id}`);
}
