import { Hono } from "hono";
import type { Context } from "hono";
import type { ArticleService } from "./article.ports.js";
import { authMiddleware } from "../../shared/web/auth-middleware.js";
import type { JwtTokenGenerator } from "../../shared/security/token.js";
import {
    createArticleSchema,
    updateArticleSchema,
    getArticlesQuerySchema,
    getFeedQuerySchema,
} from "./article.validator.js";

type Variables = {
    userId?: number;
};

export class ArticleHandler {
    constructor(
        private service: ArticleService,
        private jwtTokenGenerator: JwtTokenGenerator,
    ) {}

    getApp(): Hono<{ Variables: Variables }> {
        const app = new Hono<{ Variables: Variables }>();

        app.get("/tags", this.getTags.bind(this));

        app.get(
            "/articles",
            authMiddleware(this.jwtTokenGenerator, false),
            this.getArticles.bind(this),
        );
        app.get(
            "/articles/feed",
            authMiddleware(this.jwtTokenGenerator),
            this.getFeed.bind(this),
        );

        app.post(
            "/articles",
            authMiddleware(this.jwtTokenGenerator),
            this.createArticle.bind(this),
        );
        app.get(
            "/articles/:slug",
            authMiddleware(this.jwtTokenGenerator, false),
            this.getArticle.bind(this),
        );
        app.put(
            "/articles/:slug",
            authMiddleware(this.jwtTokenGenerator),
            this.updateArticle.bind(this),
        );
        app.delete(
            "/articles/:slug",
            authMiddleware(this.jwtTokenGenerator),
            this.deleteArticle.bind(this),
        );

        app.post(
            "/articles/:slug/favorite",
            authMiddleware(this.jwtTokenGenerator),
            this.favorite.bind(this),
        );
        app.delete(
            "/articles/:slug/favorite",
            authMiddleware(this.jwtTokenGenerator),
            this.unfavorite.bind(this),
        );

        return app;
    }

    private async getFeed(c: Context<{ Variables: Variables }>) {
        try {
            const query = getFeedQuerySchema.parse(c.req.query());
            const userId = c.get("userId");
            const result = await this.service.getFeed({
                ...query,
                userId: userId!,
            });
            return c.json({
                articles: result.articles.map((a) => this.mapArticleToResponse(a)),
                articlesCount: result.articlesCount,
            });
        } catch (err) {
            throw err;
        }
    }

    private async getArticles(c: Context<{ Variables: Variables }>) {
        try {
            const query = getArticlesQuerySchema.parse(c.req.query());
            const userId = c.get("userId");
            const result = await this.service.getArticles({
                ...query,
                observerId: userId,
            });
            return c.json({
                articles: result.articles.map((a) => this.mapArticleToResponse(a)),
                articlesCount: result.articlesCount,
            });
        } catch (err) {
            throw err;
        }
    }

    private async createArticle(c: Context<{ Variables: Variables }>) {
        try {
            const body = await c.req.json();
            const validated = createArticleSchema.parse(body);
            const { article: articleIn } = validated;
            const userId = c.get("userId");
            const article = await this.service.createArticle({
                authorId: userId!,
                title: articleIn.title,
                description: articleIn.description,
                body: articleIn.body,
                tagList: articleIn.tagList,
            });

            return c.json({ article: this.mapArticleToResponse(article) }, 201);
        } catch (err) {
            throw err;
        }
    }

    private async getArticle(c: Context<{ Variables: Variables }>) {
        try {
            const slug = c.req.param("slug")!;
            const userId = c.get("userId");
            const article = await this.service.getArticle({
                slug,
                observerId: userId,
            });
            return c.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            throw err;
        }
    }

    private async updateArticle(c: Context<{ Variables: Variables }>) {
        try {
            const body = await c.req.json();
            const validated = updateArticleSchema.parse(body);
            const { article: articleIn } = validated;
            const slug = c.req.param("slug")!;
            const userId = c.get("userId");
            const article = await this.service.updateArticle({
                slug,
                userId: userId!,
                title: articleIn.title,
                description: articleIn.description,
                body: articleIn.body,
            });

            return c.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            throw err;
        }
    }

    private async deleteArticle(c: Context<{ Variables: Variables }>) {
        try {
            const slug = c.req.param("slug")!;
            const userId = c.get("userId");
            await this.service.deleteArticle({
                slug,
                userId: userId!,
            });
            return c.json({});
        } catch (err) {
            throw err;
        }
    }

    private async favorite(c: Context<{ Variables: Variables }>) {
        try {
            const slug = c.req.param("slug")!;
            const userId = c.get("userId");
            const article = await this.service.favoriteArticle({
                slug,
                userId: userId!,
            });
            return c.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            throw err;
        }
    }

    private async unfavorite(c: Context<{ Variables: Variables }>) {
        try {
            const slug = c.req.param("slug")!;
            const userId = c.get("userId");
            const article = await this.service.unfavoriteArticle({
                slug,
                userId: userId!,
            });
            return c.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            throw err;
        }
    }

    private async getTags(c: Context<{ Variables: Variables }>) {
        try {
            const tags = await this.service.getTags();
            return c.json({ tags: tags.map((t) => t.name) });
        } catch (err) {
            throw err;
        }
    }

    private mapArticleToResponse(article: any) {
        return {
            slug: article.slug,
            title: article.title,
            description: article.description,
            body: article.body,
            tagList: article.tagList,
            createdAt: article.createdAt.toISOString(),
            updatedAt: article.updatedAt.toISOString(),
            favorited: article.favorited,
            favoritesCount: article.favoritesCount,
            author: {
                username: article.author.username,
                bio: article.author.bio,
                image: article.author.image,
                following: article.author.following,
            },
        };
    }
}
