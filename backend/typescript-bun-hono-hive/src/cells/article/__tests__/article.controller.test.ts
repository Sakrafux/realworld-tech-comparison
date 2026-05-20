import { describe, it, expect, mock, beforeEach } from "bun:test";
import { ArticleHandler } from "../article.controller.js";
import { Article } from "../article.domain.js";
import { JwtTokenGenerator } from "../../../shared/security/token.js";
import { errorHandler } from "../../../shared/web/error-handler.js";
import { Hono } from "hono";

describe("ArticleHandler", () => {
    let handler: ArticleHandler;
    let service: any;
    let jwtTokenGenerator: any;
    let app: Hono<any>;

    beforeEach(() => {
        service = {
            createArticle: mock(),
            getArticle: mock(),
            updateArticle: mock(),
            deleteArticle: mock(),
            favoriteArticle: mock(),
            unfavoriteArticle: mock(),
            getArticles: mock(),
            getFeed: mock(),
            getTags: mock(),
        };
        jwtTokenGenerator = new JwtTokenGenerator("secret");
        handler = new ArticleHandler(service, jwtTokenGenerator);
        app = handler.getApp();
        app.onError(errorHandler);
    });

    describe("createArticle", () => {
        it("should return 201 and article data on success", async () => {
            const article = new Article(1, "title", "Title", "Desc", "Body", ["tag1"], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            service.createArticle.mockResolvedValue(article);

            const token = await jwtTokenGenerator.generate(1);

            const res = await app.request("/articles", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Token ${token}`,
                },
                body: JSON.stringify({
                    article: {
                        title: "Title",
                        description: "Desc",
                        body: "Body",
                        tagList: ["tag1"],
                    },
                }),
            });

            expect(res.status).toBe(201);
            expect(await res.json()).toEqual({
                article: expect.objectContaining({
                    title: "Title",
                    slug: "title",
                }),
            });
        });
    });

    describe("getArticle", () => {
        it("should return article data", async () => {
            const article = new Article(1, "test-slug", "Title", "Desc", "Body", [], new Date(), new Date(), false, 0, { username: "author", bio: "", image: null, following: false });
            service.getArticle.mockResolvedValue(article);

            const res = await app.request("/articles/test-slug");

            expect(res.status).toBe(200);
            expect(await res.json()).toEqual({
                article: expect.objectContaining({
                    slug: "test-slug",
                }),
            });
        });
    });

    describe("getArticles", () => {
        it("should return articles list", async () => {
            const articles = [
                new Article(1, "slug1", "T1", "D1", "B1", [], new Date(), new Date(), false, 0, { username: "a", bio: "", image: null, following: false }),
            ];
            service.getArticles.mockResolvedValue({ articles, articlesCount: 1 });

            const res = await app.request("/articles?limit=10&offset=0");

            expect(res.status).toBe(200);
            expect(await res.json()).toEqual({
                articles: expect.arrayContaining([
                    expect.objectContaining({ slug: "slug1" }),
                ]),
                articlesCount: 1,
            });
        });
    });

    describe("getFeed", () => {
        it("should return articles feed", async () => {
            const articles = [
                new Article(1, "slug1", "T1", "D1", "B1", [], new Date(), new Date(), false, 0, { username: "a", bio: "", image: null, following: false }),
            ];
            service.getFeed.mockResolvedValue({ articles, articlesCount: 1 });

            const token = await jwtTokenGenerator.generate(1);
            const res = await app.request("/articles/feed?limit=10&offset=0", {
                headers: {
                    "Authorization": `Token ${token}`,
                },
            });

            expect(res.status).toBe(200);
            expect(await res.json()).toEqual({
                articles: expect.arrayContaining([
                    expect.objectContaining({ slug: "slug1" }),
                ]),
                articlesCount: 1,
            });
        });
    });
});
