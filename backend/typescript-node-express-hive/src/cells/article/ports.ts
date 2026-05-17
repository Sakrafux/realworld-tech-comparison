import type { Article, Tag } from "./article.js";
import type { User } from "../user/user.js";

export interface CreateArticleCommand {
    authorId: number;
    title: string;
    description: string;
    body: string;
    tagList: string[];
}

export interface UpdateArticleCommand {
    slug: string;
    userId: number;
    title?: string;
    description?: string;
    body?: string;
}

export interface DeleteArticleCommand {
    slug: string;
    userId: number;
}

export interface FavoriteArticleCommand {
    slug: string;
    userId: number;
}

export interface UnfavoriteArticleCommand {
    slug: string;
    userId: number;
}

export interface GetArticleQuery {
    slug: string;
    observerId?: number;
}

export interface ArticleService {
    createArticle(cmd: CreateArticleCommand): Promise<Article>;
    getArticle(query: GetArticleQuery): Promise<Article>;
    updateArticle(cmd: UpdateArticleCommand): Promise<Article>;
    deleteArticle(cmd: DeleteArticleCommand): Promise<void>;
    favoriteArticle(cmd: FavoriteArticleCommand): Promise<Article>;
    unfavoriteArticle(cmd: UnfavoriteArticleCommand): Promise<Article>;
    getTags(): Promise<Tag[]>;
}

export interface ArticleRepository {
    create(article: Article, authorId: number): Promise<void>;
    getBySlug(slug: string, observerId?: number): Promise<Article | null>;
    getByTitle(title: string, observerId?: number): Promise<Article | null>;
    update(article: Article): Promise<void>;
    delete(id: number): Promise<void>;
    favorite(articleId: number, userId: number): Promise<void>;
    unfavorite(articleId: number, userId: number): Promise<void>;
    findAllTags(): Promise<Tag[]>;
}

export interface UserProvider {
    getUser(id: number): Promise<User>;
    getUserByUsername(username: string): Promise<User>;
}
