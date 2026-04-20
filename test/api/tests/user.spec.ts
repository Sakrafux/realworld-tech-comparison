import { describe, it, expect, beforeAll } from 'vitest';
import { apiClient } from '../utils/apiClient';
import { generateUserData, unique } from '../utils/testUtils';

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
        testUser = generateUserData();
        const response = await apiClient.post<UserResponse>('/users', {
            user: testUser
        });
        userToken = response.data.user.token;
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
        });
    });
});
