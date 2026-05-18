import { describe, it, expect, beforeEach, vi } from "vitest";
import { CommentHandler } from "../handler.js";
import { Comment } from "../comment.js";
import { JwtTokenGenerator } from "../../../shared/security/token.js";

describe("CommentHandler", () => {
    let handler: CommentHandler;
    let service: any;
    let jwtTokenGenerator: any;

    beforeEach(() => {
        service = {
            createComment: vi.fn(),
            getComments: vi.fn(),
            deleteComment: vi.fn(),
        };
        jwtTokenGenerator = new JwtTokenGenerator("secret");
        handler = new CommentHandler(service, jwtTokenGenerator);
    });

    describe("createComment", () => {
        it("should return 201 and comment data on success", async () => {
            const req: any = {
                params: { slug: "test-slug" },
                userId: 1,
                body: {
                    comment: { body: "test body" },
                },
            };
            const res: any = {
                status: vi.fn().mockReturnThis(),
                json: vi.fn(),
            };
            const next = vi.fn();

            const comment = new Comment(1, new Date(), new Date(), "test body", { username: "author", bio: "", image: null, following: false });
            service.createComment.mockResolvedValue(comment);

            await (handler as any).createComment(req, res, next);

            expect(res.status).toHaveBeenCalledWith(201);
            expect(res.json).toHaveBeenCalledWith({
                comment: expect.objectContaining({
                    body: "test body",
                    id: 1,
                }),
            });
        });
    });

    describe("getComments", () => {
        it("should return comments list", async () => {
            const req: any = {
                params: { slug: "test-slug" },
                userId: undefined,
            };
            const res: any = {
                json: vi.fn(),
            };
            const next = vi.fn();

            const comments = [
                new Comment(1, new Date(), new Date(), "body1", { username: "a", bio: "", image: null, following: false }),
            ];
            service.getComments.mockResolvedValue(comments);

            await (handler as any).getComments(req, res, next);

            expect(res.json).toHaveBeenCalledWith({
                comments: expect.arrayContaining([
                    expect.objectContaining({ body: "body1" }),
                ]),
            });
        });
    });
});
