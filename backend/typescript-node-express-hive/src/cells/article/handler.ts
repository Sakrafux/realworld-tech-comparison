import { Router, type Request, type Response, type NextFunction } from "express";
import type { ArticleService } from "./ports.js";
import { type AuthenticatedRequest, authMiddleware } from "../../shared/web/auth-middleware.js";
import type { JwtTokenGenerator } from "../../shared/security/token.js";
import { createArticleSchema, updateArticleSchema } from "./validator.js";

export class ArticleHandler {
    constructor(
        private service: ArticleService,
        private jwtTokenGenerator: JwtTokenGenerator,
    ) {}

    getRouter(): Router {
        const router = Router();

        router.get("/tags", this.getTags.bind(this));

        router.post(
            "/articles",
            authMiddleware(this.jwtTokenGenerator),
            this.createArticle.bind(this),
        );
        router.get(
            "/articles/:slug",
            authMiddleware(this.jwtTokenGenerator, false),
            this.getArticle.bind(this),
        );
        router.put(
            "/articles/:slug",
            authMiddleware(this.jwtTokenGenerator),
            this.updateArticle.bind(this),
        );
        router.delete(
            "/articles/:slug",
            authMiddleware(this.jwtTokenGenerator),
            this.deleteArticle.bind(this),
        );

        router.post(
            "/articles/:slug/favorite",
            authMiddleware(this.jwtTokenGenerator),
            this.favorite.bind(this),
        );
        router.delete(
            "/articles/:slug/favorite",
            authMiddleware(this.jwtTokenGenerator),
            this.unfavorite.bind(this),
        );

        return router;
    }

    private async createArticle(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const validated = createArticleSchema.parse(req.body);
            const { article: articleIn } = validated;
            const article = await this.service.createArticle({
                authorId: req.userId!,
                title: articleIn.title,
                description: articleIn.description,
                body: articleIn.body,
                tagList: articleIn.tagList,
            });

            res.status(201).json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            next(err);
        }
    }

    private async getArticle(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const article = await this.service.getArticle({
                slug: req.params.slug as string,
                observerId: req.userId,
            });
            res.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            next(err);
        }
    }

    private async updateArticle(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const validated = updateArticleSchema.parse(req.body);
            const { article: articleIn } = validated;
            const article = await this.service.updateArticle({
                slug: req.params.slug as string,
                userId: req.userId!,
                title: articleIn.title,
                description: articleIn.description,
                body: articleIn.body,
            });

            res.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            next(err);
        }
    }

    private async deleteArticle(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            await this.service.deleteArticle({
                slug: req.params.slug as string,
                userId: req.userId!,
            });
            res.status(200).json({});
        } catch (err) {
            next(err);
        }
    }

    private async favorite(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const article = await this.service.favoriteArticle({
                slug: req.params.slug as string,
                userId: req.userId!,
            });
            res.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            next(err);
        }
    }

    private async unfavorite(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const article = await this.service.unfavoriteArticle({
                slug: req.params.slug as string,
                userId: req.userId!,
            });
            res.json({ article: this.mapArticleToResponse(article) });
        } catch (err) {
            next(err);
        }
    }

    private async getTags(_req: Request, res: Response, next: NextFunction) {
        try {
            const tags = await this.service.getTags();
            res.json({ tags: tags.map((t) => t.name) });
        } catch (err) {
            next(err);
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
