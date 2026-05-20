import { describe, it, expect, mock, beforeEach } from "bun:test";
import { CommentHandler } from "../comment.controller.js";
import { Comment } from "../comment.domain.js";
import { JwtTokenGenerator } from "../../../shared/security/token.js";
import { errorHandler } from "../../../shared/web/error-handler.js";
import { Hono } from "hono";

describe("CommentHandler", () => {
    let handler: CommentHandler;
    let service: any;
    let jwtTokenGenerator: any;
    let app: Hono<any>;

    beforeEach(() => {
        service = {
            createComment: mock(),
            getComments: mock(),
            deleteComment: mock(),
        };
        jwtTokenGenerator = new JwtTokenGenerator("secret");
        handler = new CommentHandler(service, jwtTokenGenerator);
        app = handler.getApp();
        app.onError(errorHandler);
    });

    describe("createComment", () => {
        it("should return 200 and comment data on success", async () => {
            const comment = new Comment(1, new Date(), new Date(), "test body", {
                username: "author",
                bio: "",
                image: null,
                following: false,
            });
            service.createComment.mockResolvedValue(comment);

            const token = await jwtTokenGenerator.generate(1);

            const res = await app.request("/articles/test-slug/comments", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Token ${token}`,
                },
                body: JSON.stringify({
                    comment: { body: "test body" },
                }),
            });

            expect(res.status).toBe(200);
            expect(await res.json()).toEqual({
                comment: expect.objectContaining({
                    body: "test body",
                    id: 1,
                }),
            });
        });
    });

    describe("getComments", () => {
        it("should return comments list", async () => {
            const comments = [
                new Comment(1, new Date(), new Date(), "body1", {
                    username: "a",
                    bio: "",
                    image: null,
                    following: false,
                }),
            ];
            service.getComments.mockResolvedValue(comments);

            const res = await app.request("/articles/test-slug/comments");

            expect(res.status).toBe(200);
            expect(await res.json()).toEqual({
                comments: expect.arrayContaining([expect.objectContaining({ body: "body1" })]),
            });
        });
    });
});
