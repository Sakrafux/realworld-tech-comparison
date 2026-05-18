import { describe, it, expect, beforeEach, vi } from "vitest";
import { DefaultCommentService } from "../service.js";
import { Comment } from "../comment.js";
import { ErrorType } from "../../../shared/errors/app-error.js";

describe("DefaultCommentService", () => {
    let service: DefaultCommentService;
    let repo: any;
    let userProvider: any;
    let articleProvider: any;

    beforeEach(() => {
        repo = {
            create: vi.fn(),
            findByArticleId: vi.fn(),
            getById: vi.fn(),
            delete: vi.fn(),
        };
        userProvider = {
            getUser: vi.fn(),
        };
        articleProvider = {
            getArticle: vi.fn(),
        };
        service = new DefaultCommentService(repo, userProvider, articleProvider);
    });

    describe("createComment", () => {
        it("should create a new comment successfully", async () => {
            const cmd = { slug: "test-slug", authorId: 1, body: "test body" };
            articleProvider.getArticle.mockResolvedValue({ id: 10 });
            userProvider.getUser.mockResolvedValue({ username: "author", bio: "", image: null });
            repo.create.mockImplementation(async (c: Comment) => {
                c.id = 1;
            });

            const comment = await service.createComment(cmd);

            expect(comment.id).toBe(1);
            expect(comment.body).toBe(cmd.body);
            expect(comment.author.username).toBe("author");
            expect(repo.create).toHaveBeenCalled();
        });
    });

    describe("getComments", () => {
        it("should return comments for an article", async () => {
            const query = { slug: "test-slug", observerId: 1 };
            articleProvider.getArticle.mockResolvedValue({ id: 10 });
            const comments = [new Comment(1, new Date(), new Date(), "body", { username: "a", bio: "", image: null, following: false })];
            repo.findByArticleId.mockResolvedValue(comments);

            const result = await service.getComments(query);

            expect(result).toBe(comments);
            expect(repo.findByArticleId).toHaveBeenCalledWith(10, 1);
        });
    });

    describe("deleteComment", () => {
        it("should delete comment if user is author", async () => {
            const cmd = { slug: "slug", commentId: 1, userId: 1 };
            repo.getById.mockResolvedValue({
                comment: {},
                articleId: 10,
                authorId: 1,
            });

            await service.deleteComment(cmd);

            expect(repo.delete).toHaveBeenCalledWith(1);
        });

        it("should throw forbidden if user is not author", async () => {
            const cmd = { slug: "slug", commentId: 1, userId: 2 };
            repo.getById.mockResolvedValue({
                comment: {},
                articleId: 10,
                authorId: 1,
            });

            await expect(service.deleteComment(cmd)).rejects.toMatchObject({
                type: ErrorType.Forbidden,
            });
        });
    });
});
