import { describe, it, expect, beforeEach, vi } from "vitest";
import { DefaultArticleService } from "../service.js";
import { Article } from "../article.js";
import { mockArticleRepository, mockUserProvider } from "../../../tests/mocks/article.mocks.js";
import { User } from "../../user/user.js";
import { ErrorType } from "../../../shared/errors/app-error.js";

describe("DefaultArticleService", () => {
    let service: DefaultArticleService;
    let repo: any;
    let userProvider: any;

    beforeEach(() => {
        repo = mockArticleRepository();
        userProvider = mockUserProvider();
        service = new DefaultArticleService(repo, userProvider);
    });

    describe("createArticle", () => {
        it("should create a new article successfully", async () => {
            const cmd = {
                authorId: 1,
                title: "Test Title",
                description: "Test Description",
                body: "Test Body",
                tagList: ["tag1", "tag2"],
            };
            const author = new User(1, "author", "author@example.com", "hash", "", null);
            userProvider.getUser.mockResolvedValue(author);
            repo.getBySlug.mockResolvedValue(null);
            repo.create.mockImplementation(async (a: Article) => {
                a.id = 1;
            });

            const article = await service.createArticle(cmd);

            expect(article.title).toBe(cmd.title);
            expect(article.slug).toBe("test-title");
            expect(article.author.username).toBe(author.username);
            expect(repo.create).toHaveBeenCalled();
        });

        it("should throw error if article with same slug exists", async () => {
            const cmd = {
                authorId: 1,
                title: "Test Title",
                description: "Test Description",
                body: "Test Body",
                tagList: [],
            };
            repo.getBySlug.mockResolvedValue({ id: 2 });

            await expect(service.createArticle(cmd)).rejects.toMatchObject({
                type: ErrorType.AlreadyExists,
            });
        });
    });

    describe("getArticle", () => {
        it("should return article if it exists", async () => {
            const existingArticle = new Article(1, "slug", "Title", "Desc", "Body", [], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            repo.getBySlug.mockResolvedValue(existingArticle);

            const article = await service.getArticle({ slug: "slug" });

            expect(article).toBe(existingArticle);
        });

        it("should throw error if article not found", async () => {
            repo.getBySlug.mockResolvedValue(null);

            await expect(service.getArticle({ slug: "not-found" })).rejects.toMatchObject({
                type: ErrorType.NotFound,
            });
        });
    });

    describe("updateArticle", () => {
        it("should update article fields", async () => {
            const author = new User(1, "author", "author@example.com", "hash", "", null);
            const existingArticle = new Article(1, "old-slug", "Old Title", "Old Desc", "Old Body", [], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            
            repo.getBySlug.mockImplementation(async (slug: string) => {
                if (slug === "old-slug") return existingArticle;
                return null;
            });
            userProvider.getUserByUsername.mockResolvedValue(author);
            repo.update.mockResolvedValue(undefined);

            const cmd = {
                slug: "old-slug",
                userId: 1,
                title: "New Title",
                description: "New Desc",
            };

            const updated = await service.updateArticle(cmd);

            expect(updated.title).toBe("New Title");
            expect(updated.slug).toBe("new-title");
            expect(updated.description).toBe("New Desc");
            expect(repo.update).toHaveBeenCalled();
        });

        it("should throw forbidden if user is not the author", async () => {
            const author = new User(1, "author", "author@example.com", "hash", "", null);
            const existingArticle = new Article(1, "slug", "Title", "Desc", "Body", [], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            
            repo.getBySlug.mockResolvedValue(existingArticle);
            userProvider.getUserByUsername.mockResolvedValue(author);

            const cmd = {
                slug: "slug",
                userId: 2, // Different user
                title: "New Title",
            };

            await expect(service.updateArticle(cmd)).rejects.toMatchObject({
                type: ErrorType.Forbidden,
            });
        });
    });

    describe("deleteArticle", () => {
        it("should delete article if user is author", async () => {
            const author = new User(1, "author", "author@example.com", "hash", "", null);
            const existingArticle = new Article(1, "slug", "Title", "Desc", "Body", [], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            
            repo.getBySlug.mockResolvedValue(existingArticle);
            userProvider.getUserByUsername.mockResolvedValue(author);
            repo.delete.mockResolvedValue(undefined);

            await service.deleteArticle({ slug: "slug", userId: 1 });

            expect(repo.delete).toHaveBeenCalledWith(1);
        });
    });
});
