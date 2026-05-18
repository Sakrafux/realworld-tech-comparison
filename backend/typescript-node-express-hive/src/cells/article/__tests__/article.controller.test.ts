import { describe, it, expect, vi, beforeEach } from "vitest";
import { ArticleHandler } from "../article.controller.js";
import { Article } from "../article.domain.js";
import { JwtTokenGenerator } from "../../../shared/security/token.js";


describe("ArticleHandler", () => {
    let handler: ArticleHandler;
    let service: any;
    let jwtTokenGenerator: any;

    beforeEach(() => {
        service = {
            createArticle: vi.fn(),
            getArticle: vi.fn(),
            updateArticle: vi.fn(),
            deleteArticle: vi.fn(),
            favoriteArticle: vi.fn(),
            unfavoriteArticle: vi.fn(),
            getArticles: vi.fn(),
            getFeed: vi.fn(),
            getTags: vi.fn(),
        };
        jwtTokenGenerator = new JwtTokenGenerator("secret");
        handler = new ArticleHandler(service, jwtTokenGenerator);
    });

    describe("createArticle", () => {
        it("should return 201 and article data on success", async () => {
            const req: any = {
                userId: 1,
                body: {
                    article: {
                        title: "Title",
                        description: "Desc",
                        body: "Body",
                        tagList: ["tag1"],
                    },
                },
            };
            const res: any = {
                status: vi.fn().mockReturnThis(),
                json: vi.fn(),
            };
            const next = vi.fn();

            const article = new Article(1, "title", "Title", "Desc", "Body", ["tag1"], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            service.createArticle.mockResolvedValue(article);

            await (handler as any).createArticle(req, res, next);

            expect(res.status).toHaveBeenCalledWith(201);
            expect(res.json).toHaveBeenCalledWith({
                article: expect.objectContaining({
                    title: "Title",
                    slug: "title",
                }),
            });
        });
    });

    describe("getArticle", () => {
        it("should return article data", async () => {
            const req: any = {
                params: { slug: "test-slug" },
                userId: undefined,
            };
            const res: any = {
                json: vi.fn(),
            };
            const next = vi.fn();

            const article = new Article(1, "test-slug", "Title", "Desc", "Body", [], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            service.getArticle.mockResolvedValue(article);

            await (handler as any).getArticle(req, res, next);

            expect(res.json).toHaveBeenCalledWith({
                article: expect.objectContaining({
                    slug: "test-slug",
                }),
            });
        });
    });

    describe("getArticles", () => {
        it("should return articles list", async () => {
            const req: any = {
                query: { limit: "10", offset: "0" },
                userId: undefined,
            };
            const res: any = {
                json: vi.fn(),
            };
            const next = vi.fn();

            const articles = [
                new Article(1, "slug1", "T1", "D1", "B1", [], new Date(), new Date(), false, 0, { username: "a", bio: "", image: null, following: false }),
            ];
            service.getArticles.mockResolvedValue({ articles, articlesCount: 1 });

            await (handler as any).getArticles(req, res, next);

            expect(res.json).toHaveBeenCalledWith({
                articles: expect.arrayContaining([
                    expect.objectContaining({ slug: "slug1" }),
                ]),
                articlesCount: 1,
            });
        });
    });

    describe("getFeed", () => {
        it("should return articles feed", async () => {
            const req: any = {
                query: { limit: "10", offset: "0" },
                userId: 1,
            };
            const res: any = {
                json: vi.fn(),
            };
            const next = vi.fn();

            const articles = [
                new Article(1, "slug1", "T1", "D1", "B1", [], new Date(), new Date(), false, 0, { username: "a", bio: "", image: null, following: false }),
            ];
            service.getFeed.mockResolvedValue({ articles, articlesCount: 1 });

            await (handler as any).getFeed(req, res, next);

            expect(res.json).toHaveBeenCalledWith({
                articles: expect.arrayContaining([
                    expect.objectContaining({ slug: "slug1" }),
                ]),
                articlesCount: 1,
            });
        });
    });
});
