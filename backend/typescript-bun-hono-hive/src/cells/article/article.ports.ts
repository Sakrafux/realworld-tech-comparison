import type { Article, Tag } from "./article.domain.js";
import type { User } from "../user/user.domain.js";

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

export interface GetArticlesQuery {
    tag?: string;
    author?: string;
    favorited?: string;
    limit: number;
    offset: number;
    observerId?: number;
}

export interface GetArticlesFeedQuery {
    limit: number;
    offset: number;
    userId: number;
}

export interface ArticlesList {
    articles: Article[];
    articlesCount: number;
}

export interface ArticleService {
    createArticle(cmd: CreateArticleCommand): Promise<Article>;
    getArticle(query: GetArticleQuery): Promise<Article>;
    updateArticle(cmd: UpdateArticleCommand): Promise<Article>;
    deleteArticle(cmd: DeleteArticleCommand): Promise<void>;
    favoriteArticle(cmd: FavoriteArticleCommand): Promise<Article>;
    unfavoriteArticle(cmd: UnfavoriteArticleCommand): Promise<Article>;
    getArticles(query: GetArticlesQuery): Promise<ArticlesList>;
    getFeed(query: GetArticlesFeedQuery): Promise<ArticlesList>;
    getTags(): Promise<Tag[]>;
}

export interface ArticleRepository {
    create(article: Article, authorId: number): Promise<void>;
    getBySlug(slug: string, observerId?: number): Promise<Article | null>;
    getByTitle(title: string, observerId?: number): Promise<Article | null>;
    findAll(query: GetArticlesQuery): Promise<ArticlesList>;
    findFeed(query: GetArticlesFeedQuery): Promise<ArticlesList>;
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
