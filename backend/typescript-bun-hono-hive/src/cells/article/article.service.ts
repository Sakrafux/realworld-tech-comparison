import { Article, slugify, type Tag } from "./article.domain.js";
import type {
    CreateArticleCommand,
    UpdateArticleCommand,
    DeleteArticleCommand,
    FavoriteArticleCommand,
    UnfavoriteArticleCommand,
    GetArticleQuery,
    ArticleService,
    ArticleRepository,
    UserProvider,
    GetArticlesQuery,
    ArticlesList,
    GetArticlesFeedQuery,
} from "./article.ports.js";
import {
    newAlreadyExistsError,
    newForbiddenError,
    newResourceNotFound,
} from "../../shared/errors/app-error.js";

export class DefaultArticleService implements ArticleService {
    constructor(
        private repo: ArticleRepository,
        private userProvider: UserProvider,
    ) {}

    async createArticle(cmd: CreateArticleCommand): Promise<Article> {
        const slug = slugify(cmd.title);
        const existing = await this.repo.getBySlug(slug);
        if (existing) {
            throw newAlreadyExistsError("Article with this title/slug already exists");
        }

        const author = await this.userProvider.getUser(cmd.authorId);
        const article = new Article(
            0,
            slug,
            cmd.title,
            cmd.description,
            cmd.body,
            cmd.tagList,
            new Date(),
            new Date(),
            false,
            0,
            {
                username: author.username,
                bio: author.bio,
                image: author.image,
                following: false,
            },
        );

        await this.repo.create(article, cmd.authorId);
        return article;
    }

    async getArticle(query: GetArticleQuery): Promise<Article> {
        const article = await this.repo.getBySlug(query.slug, query.observerId);
        if (!article) {
            throw newResourceNotFound("Article", "slug", query.slug);
        }
        return article;
    }

    async updateArticle(cmd: UpdateArticleCommand): Promise<Article> {
        const article = await this.getArticle({ slug: cmd.slug, observerId: cmd.userId });

        const author = await this.userProvider.getUserByUsername(article.author.username);
        if (author.id !== cmd.userId) {
            throw newForbiddenError("You are not the author of this article");
        }

        await article.update(
            {
                title: cmd.title,
                description: cmd.description,
                body: cmd.body,
            },
            async (_title, slug) => {
                const existing = await this.repo.getBySlug(slug);
                if (existing) {
                    throw newAlreadyExistsError("Article with this title/slug already exists");
                }
            },
        );

        await this.repo.update(article);
        return article;
    }

    async deleteArticle(cmd: DeleteArticleCommand): Promise<void> {
        const article = await this.getArticle({ slug: cmd.slug, observerId: cmd.userId });
        const author = await this.userProvider.getUserByUsername(article.author.username);
        if (author.id !== cmd.userId) {
            throw newForbiddenError("You are not the author of this article");
        }

        await this.repo.delete(article.id);
    }

    async favoriteArticle(cmd: FavoriteArticleCommand): Promise<Article> {
        const article = await this.getArticle({ slug: cmd.slug, observerId: cmd.userId });
        await this.repo.favorite(article.id, cmd.userId);
        return this.getArticle({ slug: cmd.slug, observerId: cmd.userId });
    }

    async unfavoriteArticle(cmd: UnfavoriteArticleCommand): Promise<Article> {
        const article = await this.getArticle({ slug: cmd.slug, observerId: cmd.userId });
        await this.repo.unfavorite(article.id, cmd.userId);
        return this.getArticle({ slug: cmd.slug, observerId: cmd.userId });
    }

    async getArticles(query: GetArticlesQuery): Promise<ArticlesList> {
        return this.repo.findAll(query);
    }

    async getFeed(query: GetArticlesFeedQuery): Promise<ArticlesList> {
        return this.repo.findFeed(query);
    }

    async getTags(): Promise<Tag[]> {
        return this.repo.findAllTags();
    }
}
