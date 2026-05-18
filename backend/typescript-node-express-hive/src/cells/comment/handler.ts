import { Router, type Response, type NextFunction } from "express";
import type { CommentService } from "./ports.js";
import { type AuthenticatedRequest, authMiddleware } from "../../shared/web/auth-middleware.js";
import type { JwtTokenGenerator } from "../../shared/security/token.js";
import { createCommentSchema } from "./validator.js";

export class CommentHandler {
    constructor(
        private service: CommentService,
        private jwtTokenGenerator: JwtTokenGenerator,
    ) {}

    getRouter(): Router {
        const router = Router();

        router.post(
            "/articles/:slug/comments",
            authMiddleware(this.jwtTokenGenerator),
            this.createComment.bind(this),
        );
        router.get(
            "/articles/:slug/comments",
            authMiddleware(this.jwtTokenGenerator, false),
            this.getComments.bind(this),
        );
        router.delete(
            "/articles/:slug/comments/:id",
            authMiddleware(this.jwtTokenGenerator),
            this.deleteComment.bind(this),
        );

        return router;
    }

    private async createComment(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const validated = createCommentSchema.parse(req.body);
            const comment = await this.service.createComment({
                slug: req.params.slug as string,
                authorId: req.userId!,
                body: validated.comment.body,
            });

            res.status(200).json({ comment: this.mapCommentToResponse(comment) });
        } catch (err) {
            next(err);
        }
    }

    private async getComments(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const comments = await this.service.getComments({
                slug: req.params.slug as string,
                observerId: req.userId,
            });

            res.json({ comments: comments.map((c) => this.mapCommentToResponse(c)) });
        } catch (err) {
            next(err);
        }
    }

    private async deleteComment(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            await this.service.deleteComment({
                slug: req.params.slug as string,
                commentId: parseInt(req.params.id as string),
                userId: req.userId!,
            });

            res.status(200).json({});
        } catch (err) {
            next(err);
        }
    }

    private mapCommentToResponse(comment: any) {
        return {
            id: comment.id,
            createdAt: comment.createdAt.toISOString(),
            updatedAt: comment.updatedAt.toISOString(),
            body: comment.body,
            author: {
                username: comment.author.username,
                bio: comment.author.bio,
                image: comment.author.image,
                following: comment.author.following,
            },
        };
    }
}
