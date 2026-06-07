import type { Profile } from "@/api/features/profile-api.ts";
import api from "@/api/api.ts";

export type NewArticle = {
    title: string;
    description: string;
    body: string;
    tagList?: string[];
};

export type UpdateArticle = {
    title: string;
    description: string;
    body: string;
};

export type NewArticleRequest = {
    article: NewArticle;
};

export type UpdateArticleRequest = {
    article: UpdateArticle;
};

export type Article = {
    slug: string;
    title: string;
    description: string;
    body: string;
    tagList: string[];
    createdAt: string; // date-time
    updatedAt: string; // date-time
    favorited: boolean;
    favoritesCount: number;
    author: Profile;
};

export type SingleArticleResponse = {
    article: Article;
};

export type MultipleArticlesResponse = {
    articles: Article[];
    articlesCount: number;
};

export async function getArticles(
    params: {
        tag?: string;
        author?: string;
        favorited?: string;
        offset?: number;
        limit?: number;
    } = {},
): Promise<MultipleArticlesResponse> {
    const actualParams = Object.fromEntries(
        Object.entries(params)
            .filter(([, value]) => value)
            .map(([key, value]) => [key, value.toString()]),
    );

    return api.get<MultipleArticlesResponse>("/articles", actualParams);
}

export async function getArticlesFeed(
    params: {
        offset?: number;
        limit?: number;
    } = {},
): Promise<MultipleArticlesResponse> {
    const actualParams = Object.fromEntries(
        Object.entries(params)
            .filter(([, value]) => value)
            .map(([key, value]) => [key, value.toString()]),
    );

    return api.get<MultipleArticlesResponse>("/articles/feed", actualParams);
}

export async function createArticleFavorite(slug: string): Promise<SingleArticleResponse> {
    return api.post(`/articles/${slug}/favorite`);
}

export async function deleteArticleFavorite(slug: string): Promise<SingleArticleResponse> {
    return api.delete(`/articles/${slug}/favorite`);
}

export async function createArticle(newArticle: NewArticle): Promise<SingleArticleResponse> {
    return api.post(`/articles`, { article: newArticle });
}

export async function updateArticle(
    slug: string,
    updateArticle: UpdateArticle,
): Promise<SingleArticleResponse> {
    return api.put(`/articles/${slug}`, { article: updateArticle });
}

export async function getArticle(slug: string): Promise<SingleArticleResponse> {
    return api.get(`/articles/${slug}`);
}

export async function deleteArticle(slug: string): Promise<void> {
    return api.delete(`/articles/${slug}`);
}
