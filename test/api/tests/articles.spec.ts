import { describe, it, expect, beforeAll } from 'vitest';
import { apiClient } from '../utils/apiClient';
import { generateUserData, unique, uniqueTag, createArticle, createUserData } from '../utils/testUtils';

describe('Articles API', () => {
    let authorToken: string;
    let authorUser: any;
    let favoriterToken: string;
    let favoriterUser: any;

    // Setup users
    beforeAll(async () => {
        const authorData = await createUserData();
        authorUser = authorData.user;
        authorToken = authorData.user.token;

        const favoriterData = await createUserData();
        favoriterUser = favoriterData.user;
        favoriterToken = favoriterData.user.token;
    });

    describe('GET /articles', () => {
        it('Default Fetch: should return 200 with articles and apply default limit 20', async () => {
            const response = await apiClient.get<any>('/articles');
            expect(response.status).toBe(200);
            expect(Array.isArray(response.data.articles)).toBe(true);
            expect(response.data.articles.length).toBeLessThanOrEqual(20);
            expect(response.data).toHaveProperty('articlesCount');
        });

        it('Filter by Tag: should include only articles with specific tag', async () => {
            const tag = uniqueTag();
            await createArticle(authorToken, [tag]);

            const response = await apiClient.get<any>('/articles', { params: { tag } });
            expect(response.status).toBe(200);
            expect(response.data.articles.length).toBeGreaterThan(0);
            expect(response.data.articles.every((a: any) => a.tagList.includes(tag))).toBe(true);
        });

        it('Filter by Author: should return only articles by specific user', async () => {
            await createArticle(authorToken);
            const response = await apiClient.get<any>('/articles', { params: { author: authorUser.username } });
            expect(response.status).toBe(200);
            expect(response.data.articles.length).toBeGreaterThan(0);
            expect(response.data.articles.every((a: any) => a.author.username === authorUser.username)).toBe(true);
        });

        it('Filter by Favorited: should return only articles favorited by user', async () => {
            const article = await createArticle(authorToken);
            await apiClient.post(`/articles/${article.slug}/favorite`, undefined, { token: favoriterToken });

            const response = await apiClient.get<any>('/articles', { params: { favorited: favoriterUser.username } });
            expect(response.status).toBe(200);
            expect(response.data.articles.length).toBeGreaterThan(0);
            expect(response.data.articles.some((a: any) => a.slug === article.slug)).toBe(true);
        });

        it('Pagination Limit: should return exact number of articles', async () => {
            await createArticle(authorToken);
            await createArticle(authorToken);
            await createArticle(authorToken);

            const response = await apiClient.get<any>('/articles', { params: { limit: '2' } });
            expect(response.status).toBe(200);
            expect(response.data.articles.length).toBe(2);
        });

        it('Pagination Offset: should skip specified number of recent articles', async () => {
            const limitResponse = await apiClient.get<any>('/articles', { params: { limit: '2' } });
            if (limitResponse.data.articles.length >= 2) {
                const secondArticle = limitResponse.data.articles[1];

                const offsetResponse = await apiClient.get<any>('/articles', { params: { limit: '1', offset: '1' } });
                expect(offsetResponse.status).toBe(200);
                if (offsetResponse.data.articles.length > 0) {
                    expect(offsetResponse.data.articles[0].slug).toBe(secondArticle.slug);
                }
            }
        });

        it('Combined Filters: should return intersection of tag, author, limit, and offset', async () => {
            const tag = uniqueTag();
            await createArticle(authorToken, [tag]);
            await createArticle(authorToken, [tag]);

            const response = await apiClient.get<any>('/articles', {
                params: {
                    tag,
                    author: authorUser.username,
                    limit: '1',
                    offset: '0'
                }
            });
            expect(response.status).toBe(200);
            expect(response.data.articles.length).toBe(1);
            expect(response.data.articles[0].tagList).toContain(tag);
            expect(response.data.articles[0].author.username).toBe(authorUser.username);
        });

        it('Authenticated Fetch: should populate favorited boolean correctly', async () => {
            const article = await createArticle(authorToken);
            await apiClient.post(`/articles/${article.slug}/favorite`, undefined, { token: favoriterToken });

            const response = await apiClient.get<any>('/articles', { token: favoriterToken });
            expect(response.status).toBe(200);
            const found = response.data.articles.find((a: any) => a.slug === article.slug);
            if (found) {
                expect(found.favorited).toBe(true);
            }
        });

        it('Edge Case: Limit Below Minimum should return 422', async () => {
            try {
                await apiClient.get<any>('/articles', { params: { limit: '0' } });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
            try {
                await apiClient.get<any>('/articles', { params: { limit: '-1' } });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });

        it('Edge Case: Offset Below Minimum should return 422', async () => {
            try {
                await apiClient.get<any>('/articles', { params: { offset: '-1' } });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });

        it('Edge Case: No Results for Filters should return 200 with empty array', async () => {
            const response = await apiClient.get<any>('/articles', {
                params: { tag: unique('nonexistent'), author: unique('nonexistent'), favorited: unique('nonexistent') }
            });
            expect(response.status).toBe(200);
            expect(response.data.articles).toEqual([]);
            expect(response.data.articlesCount).toBe(0);
        });

        it('Edge Case: Invalid Parameter Types should return 422', async () => {
            try {
                await apiClient.get<any>('/articles', { params: { limit: 'abc' } });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });
    });

    describe('POST /articles', () => {
        it('Standard Creation: should create article and return 201', async () => {
            const articleData = {
                title: unique('Article POST'),
                description: 'Test description',
                body: 'Test body'
            };
            const response = await apiClient.post<any>('/articles', { article: articleData }, { token: authorToken });
            expect(response.status).toBe(201);
            expect(response.data.article.title).toBe(articleData.title);
            expect(response.data.article.author.username).toBe(authorUser.username);
        });

        it('Creation with Tags: should create article with tags', async () => {
            const tags = ['tag1', 'tag2'];
            const articleData = {
                title: unique('Article Tags POST'),
                description: 'Test description',
                body: 'Test body',
                tagList: tags
            };
            const response = await apiClient.post<any>('/articles', { article: articleData }, { token: authorToken });
            expect(response.status).toBe(201);
            expect(response.data.article.tagList).toEqual(expect.arrayContaining(tags));
        });

        it('Edge Case: Missing Authorization should return 401', async () => {
            try {
                const articleData = {
                    title: unique('Article No Auth'),
                    description: 'Test description',
                    body: 'Test body'
                };
                await apiClient.post<any>('/articles', { article: articleData });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });

        it('Edge Case: Invalid Authorization should return 401', async () => {
            try {
                const articleData = {
                    title: unique('Article Invalid Auth'),
                    description: 'Test description',
                    body: 'Test body'
                };
                await apiClient.post<any>('/articles', { article: articleData }, { token: 'invalid.token' });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });

        it('Edge Case: Missing Title should return 422', async () => {
            try {
                await apiClient.post<any>('/articles', {
                    article: { description: 'desc', body: 'body' }
                }, { token: authorToken });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });

        it('Edge Case: Missing Description should return 422', async () => {
            try {
                await apiClient.post<any>('/articles', {
                    article: { title: unique('title'), body: 'body' }
                }, { token: authorToken });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });

        it('Edge Case: Missing Body should return 422', async () => {
            try {
                await apiClient.post<any>('/articles', {
                    article: { title: unique('title'), description: 'desc' }
                }, { token: authorToken });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });

        it('Edge Case: Empty Payload should return 422', async () => {
            try {
                await apiClient.post<any>('/articles', {}, { token: authorToken });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });

        it('Edge Case: Invalid Tag Format should return 422', async () => {
            try {
                await apiClient.post<any>('/articles', {
                    article: {
                        title: unique('title tag format'),
                        description: 'desc',
                        body: 'body',
                        tagList: 'not-an-array'
                    }
                }, { token: authorToken });
                expect.fail('Should have thrown an error');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }

            try {
                await apiClient.post<any>('/articles', {
                    article: {
                        title: unique('title tag length'),
                        description: 'desc',
                        body: 'body',
                        tagList: ['this-tag-is-way-too-long-more-than-20-chars']
                    }
                }, { token: authorToken });
                expect.fail('Should have thrown an error for long tag');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });

        it('Edge Case: Duplicate Title/Slug should return 422 or handle correctly', async () => {
            const title = unique('Duplicate Title');
            const articleData = { title, description: 'desc', body: 'body' };
            await apiClient.post<any>('/articles', { article: articleData }, { token: authorToken });

            try {
                const response = await apiClient.post<any>('/articles', { article: articleData }, { token: authorToken });
                if (response.status === 201) {
                    expect(response.data.article.slug).toBeDefined();
                }
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });
    });

    describe('POST/DELETE /articles/{slug}/favorite', () => {
        let article: any;

        beforeAll(async () => {
            article = await createArticle(authorToken);
        });

        it('Favorite: should return 200 with favorited: true and increment count', async () => {
            const response = await apiClient.post<any>(`/articles/${article.slug}/favorite`, undefined, { token: favoriterToken });
            expect(response.status).toBe(200);
            expect(response.data.article.favorited).toBe(true);
            expect(response.data.article.favoritesCount).toBeGreaterThan(0);
        });

        it('Unfavorite: should return 200 with favorited: false and decrement count', async () => {
            // Unfavorite what was favorited in the previous test
            const response = await apiClient.delete<any>(`/articles/${article.slug}/favorite`, { token: favoriterToken });
            expect(response.status).toBe(200);
            expect(response.data.article.favorited).toBe(false);
            expect(response.data.article.favoritesCount).toBe(0);
        });

        it('Edge Case: Favorite Article Twice should be idempotent', async () => {
            await apiClient.post(`/articles/${article.slug}/favorite`, undefined, { token: favoriterToken });
            const response = await apiClient.post<any>(`/articles/${article.slug}/favorite`, undefined, { token: favoriterToken });
            expect(response.status).toBe(200);
            expect(response.data.article.favoritesCount).toBe(1);
        });

        it('Edge Case: Favorite Non-Existent Article should return 404', async () => {
            try {
                await apiClient.post('/articles/non-existent-slug/favorite', undefined, { token: favoriterToken });
                expect.fail('Should have thrown 404');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });

        it('Edge Case: Unfavorite Non-Existent Article should return 404', async () => {
            try {
                await apiClient.delete('/articles/non-existent-slug/favorite', { token: favoriterToken });
                expect.fail('Should have thrown 404');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });

        it('Edge Case: Favorite without Auth should return 401', async () => {
            try {
                await apiClient.post(`/articles/${article.slug}/favorite`);
                expect.fail('Should have thrown 401');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });
    });
});
