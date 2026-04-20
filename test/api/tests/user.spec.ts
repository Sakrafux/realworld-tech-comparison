import { describe, it, expect, beforeAll } from 'vitest';
import { apiClient } from '../utils/apiClient';
import { generateUserData, unique, createUserData } from '../utils/testUtils';

interface UserResponse {
    user: {
        email: string;
        token: string;
        username: string;
        bio: string | null;
        image: string | null;
    }
}

describe('User API', () => {
    let testUser: any;
    let userToken: string;

    beforeAll(async () => {
        // Create a user to use in the tests
        const { user } = await createUserData();
        testUser = user;
        userToken = user.token;
    });

    describe('GET /user', () => {
        it('should get current user with valid token', async () => {
            const response = await apiClient.get<UserResponse>('/user', {
                token: userToken
            });

            expect(response.status).toBe(200);
            expect(response.data.user.email).toBe(testUser.email);
            expect(response.data.user.username).toBe(testUser.username);
        });

        it('should return 401 without token', async () => {
            try {
                await apiClient.get('/user');
                expect.fail('Should have thrown 401');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });
    });

    describe('PUT /user', () => {
        it('should update user bio and image', async () => {
            const newBio = 'I am a software engineer';
            const newImage = 'http://example.com/image.jpg';

            const response = await apiClient.put<UserResponse>('/user',
                {
                    user: {
                        bio: newBio,
                        image: newImage
                    }
                },
                { token: userToken }
            );

            expect(response.status).toBe(200);
            expect(response.data.user.bio).toBe(newBio);
            expect(response.data.user.image).toBe(newImage);
        });

        it('should update username', async () => {
            const newUsername = unique('newuser');
            const response = await apiClient.put<UserResponse>('/user',
                {
                    user: {
                        username: newUsername
                    }
                },
                { token: userToken }
            );

            expect(response.status).toBe(200);
            expect(response.data.user.username).toBe(newUsername);
            // Update our local reference for subsequent tests
            testUser.username = newUsername;
        });

        it('should return 422 for duplicate email during update', async () => {
            const { user } = await createUserData();

            try {
                await apiClient.put('/user',
                    { user: { email: user.email } },
                    { token: userToken }
                );
                expect.fail('Should have thrown 422 for duplicate email');
            } catch (error: any) {
                expect(error.status).toBe(422);
                expect(error.data.errors.body).toContain('Email already exists');
            }
        });

        it('should return 422 for duplicate username during update', async () => {
            const { user } = await createUserData();

            try {
                await apiClient.put('/user',
                    { user: { username: user.username } },
                    { token: userToken }
                );
                expect.fail('Should have thrown 422 for duplicate username');
            } catch (error: any) {
                expect(error.status).toBe(422);
                expect(error.data.errors.body).toContain('Username already exists');
            }
        });

        it('should return 401 without token during update', async () => {
            try {
                await apiClient.put('/user', { user: { bio: 'test' } });
                expect.fail('Should have thrown 401 without token');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });

        it('should return 422 for invalid email during update', async () => {
            try {
                await apiClient.put('/user',
                    { user: { email: 'not-an-email' } },
                    { token: userToken }
                );
                expect.fail('Should have thrown 422 for invalid email');
            } catch (error: any) {
                expect(error.status).toBe(422);
                const allErrors = error.data.errors.body.join(' ');
                expect(allErrors.toLowerCase()).toContain('email');
            }
        });

        it('should return 422 for short password during update', async () => {
            try {
                await apiClient.put('/user',
                    { user: { password: 'short' } },
                    { token: userToken }
                );
                expect.fail('Should have thrown 422 for short password');
            } catch (error: any) {
                expect(error.status).toBe(422);
                const allErrors = error.data.errors.body.join(' ');
                expect(allErrors.toLowerCase()).toContain('password');
            }
        });
    });
});
