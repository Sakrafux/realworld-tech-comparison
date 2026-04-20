import { describe, it, expect, beforeAll } from 'vitest';
import { apiClient } from '../utils/apiClient';
import { generateUserData } from '../utils/testUtils';

interface ProfileResponse {
    profile: {
        username: string;
        bio: string | null;
        image: string | null;
        following: boolean;
    }
}

interface UserResponse {
    user: {
        email: string;
        token: string;
        username: string;
        bio: string | null;
        image: string | null;
    }
}

describe('Profiles API', () => {
    let targetUser: any;
    let followerUser: any;
    let followerToken: string;

    beforeAll(async () => {
        // Create target user
        targetUser = generateUserData();
        await apiClient.post<UserResponse>('/users', {
            user: targetUser
        });

        // Create follower user
        followerUser = generateUserData();
        const response = await apiClient.post<UserResponse>('/users', {
            user: followerUser
        });
        followerToken = response.data.user.token;
    });

    describe('GET /profiles/:username', () => {
        it('should get profile without authentication', async () => {
            const response = await apiClient.get<ProfileResponse>(`/profiles/${targetUser.username}`);

            expect(response.status).toBe(200);
            expect(response.data.profile.username).toBe(targetUser.username);
            expect(response.data.profile.following).toBe(false);
        });

        it('should get profile with authentication (not following)', async () => {
            const response = await apiClient.get<ProfileResponse>(`/profiles/${targetUser.username}`, {
                token: followerToken
            });

            expect(response.status).toBe(200);
            expect(response.data.profile.username).toBe(targetUser.username);
            expect(response.data.profile.following).toBe(false);
        });

        it('should return 404 for non-existent user', async () => {
            try {
                await apiClient.get('/profiles/non-existent-user');
                expect.fail('Should have thrown 404');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });
    });

    describe('POST /profiles/:username/follow', () => {
        it('should follow a user', async () => {
            const response = await apiClient.post<ProfileResponse>(`/profiles/${targetUser.username}/follow`,
                {},
                { token: followerToken }
            );

            expect(response.status).toBe(200);
            expect(response.data.profile.username).toBe(targetUser.username);
            expect(response.data.profile.following).toBe(true);

            // Verify with GET
            const getResponse = await apiClient.get<ProfileResponse>(`/profiles/${targetUser.username}`, {
                token: followerToken
            });
            expect(getResponse.data.profile.following).toBe(true);
        });

        it('should return 401 without authentication', async () => {
            try {
                await apiClient.post(`/profiles/${targetUser.username}/follow`);
                expect.fail('Should have thrown 401');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });

        it('should return 404 for following non-existent user', async () => {
            try {
                await apiClient.post('/profiles/non-existent-user/follow', {}, { token: followerToken });
                expect.fail('Should have thrown 404');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });
    });

    describe('DELETE /profiles/:username/follow', () => {
        it('should unfollow a user', async () => {
            // First ensure we are following
            const followResponse = await apiClient.post(`/profiles/${targetUser.username}/follow`, {}, { token: followerToken });

            expect(followResponse.status).toBe(200);
            expect(followResponse.data.profile.username).toBe(targetUser.username);
            expect(followResponse.data.profile.following).toBe(true);

            // Then unfollow
            const response = await apiClient.delete<ProfileResponse>(`/profiles/${targetUser.username}/follow`, {
                token: followerToken
            });

            expect(response.status).toBe(200);
            expect(response.data.profile.username).toBe(targetUser.username);
            expect(response.data.profile.following).toBe(false);

            // Verify with GET
            const getResponse = await apiClient.get<ProfileResponse>(`/profiles/${targetUser.username}`, {
                token: followerToken
            });
            expect(getResponse.data.profile.following).toBe(false);
        });

        it('should return 401 without authentication', async () => {
            try {
                await apiClient.delete(`/profiles/${targetUser.username}/follow`);
                expect.fail('Should have thrown 401');
            } catch (error: any) {
                expect(error.status).toBe(401);
            }
        });

        it('should return 404 for unfollowing non-existent user', async () => {
            try {
                await apiClient.delete('/profiles/non-existent-user/follow', { token: followerToken });
                expect.fail('Should have thrown 404');
            } catch (error: any) {
                expect(error.status).toBe(404);
            }
        });
    });
});
