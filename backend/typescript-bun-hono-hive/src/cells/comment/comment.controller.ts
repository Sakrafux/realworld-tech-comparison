import { Hono } from "hono";
import type { Context } from "hono";
import type { CommentService } from "./comment.ports.js";
import { authMiddleware } from "../../shared/web/auth-middleware.js";
import type { JwtTokenGenerator } from "../../shared/security/token.js";
import { createCommentSchema } from "./comment.validator.js";

type Variables = {
    userId?: number;
};

export class CommentHandler {
    constructor(
        private service: CommentService,
        private jwtTokenGenerator: JwtTokenGenerator,
    ) {}

    getApp(): Hono<{ Variables: Variables }> {
        const app = new Hono<{ Variables: Variables }>();

        app.post(
            "/articles/:slug/comments",
            authMiddleware(this.jwtTokenGenerator),
            this.createComment.bind(this),
        );
        app.get(
            "/articles/:slug/comments",
            authMiddleware(this.jwtTokenGenerator, false),
            this.getComments.bind(this),
        );
        app.delete(
            "/articles/:slug/comments/:id",
            authMiddleware(this.jwtTokenGenerator),
            this.deleteComment.bind(this),
        );

        return app;
    }

    private async createComment(c: Context<{ Variables: Variables }>) {
        try {
            const body = await c.req.json();
            const validated = createCommentSchema.parse(body);
            const slug = c.req.param("slug")!;
            const userId = c.get("userId");
            const comment = await this.service.createComment({
                slug,
                authorId: userId!,
                body: validated.comment.body,
            });

            return c.json({ comment: this.mapCommentToResponse(comment) });
        } catch (err) {
            throw err;
        }
    }

    private async getComments(c: Context<{ Variables: Variables }>) {
        try {
            const slug = c.req.param("slug")!;
            const userId = c.get("userId");
            const comments = await this.service.getComments({
                slug,
                observerId: userId,
            });

            return c.json({ comments: comments.map((c) => this.mapCommentToResponse(c)) });
        } catch (err) {
            throw err;
        }
    }

    private async deleteComment(c: Context<{ Variables: Variables }>) {
        try {
            const slug = c.req.param("slug")!;
            const commentId = parseInt(c.req.param("id")!);
            const userId = c.get("userId");
            await this.service.deleteComment({
                slug,
                commentId,
                userId: userId!,
            });

            return c.json({});
        } catch (err) {
            throw err;
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
