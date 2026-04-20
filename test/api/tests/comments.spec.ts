import { describe, it, expect, beforeAll } from 'vitest';
import { apiClient } from '../utils/apiClient';
import { unique, createArticle, createUserData } from '../utils/testUtils';

describe('Comments API', () => {
    let authorToken: string;
    let authorUser: any;
    let commenterToken: string;
    let commenterUser: any;
    let article: any;

    beforeAll(async () => {
        const authorData = await createUserData();
        authorUser = authorData.user;
        authorToken = authorData.user.token;

        const commenterData = await createUserData();
        commenterUser = commenterData.user;
        commenterToken = commenterData.user.token;

        article = await createArticle(authorToken);
    });

    describe('POST /articles/{slug}/comments', () => {
        it('Add Comment: should return 200 with comment', async () => {
            const commentData = { body: 'This is a test comment' };
            const response = await apiClient.post<any>(`/articles/${article.slug}/comments`, { comment: commentData }, { token: commenterToken });
            expect(response.status).toBe(200);
            expect(response.data.comment.body).toBe(commentData.body);
            expect(response.data.comment.author.username).toBe(commenterUser.username);
        });

        it('Edge Case: Add Comment without Auth should return 401', async () => {
            try {
                await apiClient.post(`/articles/${article.slug}/comments`, { comment: { body: 'fail' } });
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });

        it('Edge Case: Add Comment to Non-Existent Article should return 404', async () => {
            try {
                await apiClient.post('/articles/non-existent/comments', { comment: { body: 'fail' } }, { token: commenterToken });
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });

        it('Edge Case: Add Comment with missing body should return 422', async () => {
            try {
                await apiClient.post(`/articles/${article.slug}/comments`, { comment: {} }, { token: commenterToken });
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });
    });

    describe('GET /articles/{slug}/comments', () => {
        it('Get Comments: should return 200 with list of comments', async () => {
            const response = await apiClient.get<any>(`/articles/${article.slug}/comments`);
            expect(response.status).toBe(200);
            expect(Array.isArray(response.data.comments)).toBe(true);
            expect(response.data.comments.length).toBeGreaterThan(0);
        });

        it('Edge Case: Get Comments for Non-Existent Article should return 404', async () => {
            try {
                await apiClient.get('/articles/non-existent/comments');
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });
    });

    describe('DELETE /articles/{slug}/comments/{id}', () => {
        it('Delete Comment: should return 200 and comment should be gone', async () => {
            const addResponse = await apiClient.post<any>(`/articles/${article.slug}/comments`, { comment: { body: 'to be deleted' } }, { token: commenterToken });
            const commentId = addResponse.data.comment.id;

            const deleteResponse = await apiClient.delete(`/articles/${article.slug}/comments/${commentId}`, { token: commenterToken });
            expect(deleteResponse.status).toBe(200);

            const getResponse = await apiClient.get<any>(`/articles/${article.slug}/comments`);
            expect(getResponse.data.comments.some((c: any) => c.id === commentId)).toBe(false);
        });

        it('Edge Case: Delete Comment as Non-Author should return 401/403', async () => {
            const addResponse = await apiClient.post<any>(`/articles/${article.slug}/comments`, { comment: { body: 'not yours' } }, { token: authorToken });
            const commentId = addResponse.data.comment.id;

            try {
                await apiClient.delete(`/articles/${article.slug}/comments/${commentId}`, { token: commenterToken });
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status === 401 || error.status === 403).toBe(true);
            }
        });

        it('Edge Case: Delete Non-Existent Comment should return 404', async () => {
            try {
                await apiClient.delete(`/articles/${article.slug}/comments/999999`, { token: commenterToken });
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });

        it('Edge Case: Delete Comment from Non-Existent Article should return 404', async () => {
            try {
                await apiClient.delete('/articles/non-existent/comments/1', { token: commenterToken });
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });

        it('Edge Case: Delete Comment without Auth should return 401', async () => {
            try {
                await apiClient.delete(`/articles/${article.slug}/comments/1`);
                expect.fail('Should have failed');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });
    });
});
