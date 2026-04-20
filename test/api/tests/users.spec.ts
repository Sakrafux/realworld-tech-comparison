import { describe, it, expect } from 'vitest';
import { apiClient } from '../utils/apiClient';
import { generateUserData } from '../utils/testUtils';

interface UserResponse {
    user: {
        email: string;
        token: string;
        username: string;
        bio: string | null;
        image: string | null;
    }
}

describe('Users API', () => {
    describe('POST /users (Registration)', () => {
        it('should register a new user successfully', async () => {
            const userData = generateUserData();
            const response = await apiClient.post<UserResponse>('/users', {
                user: userData
            });

            expect(response.status).toBe(201);
            expect(response.data.user.username).toBe(userData.username);
            expect(response.data.user.email).toBe(userData.email);
            expect(response.data.user.token).toBeDefined();
        });

        it('should return 422 for duplicate email', async () => {
            const userData = generateUserData();
            // First registration
            await apiClient.post('/users', { user: userData });

            // Second registration with same email
            try {
                await apiClient.post('/users', {
                    user: {
                        username: 'different-user',
                        email: userData.email,
                        password: 'password123'
                    }
                });
                expect.fail('Should have thrown an error for duplicate email');
            } catch (error: any) {
                expect(error.status).toBe(422);
                expect(error.data.errors.body).toContain('Email already exists');
            }
        });
    });

    describe('POST /users/login (Authentication)', () => {
        it('should login an existing user successfully', async () => {
            const userData = generateUserData();
            await apiClient.post('/users', { user: userData });

            const response = await apiClient.post<UserResponse>('/users/login', {
                user: {
                    email: userData.email,
                    password: userData.password
                }
            });

            expect(response.status).toBe(200);
            expect(response.data.user.email).toBe(userData.email);
            expect(response.data.user.token).toBeDefined();
        });

        it('should return 401 for wrong credentials', async () => {
            const userData = generateUserData();
            await apiClient.post('/users', { user: userData });

            try {
                await apiClient.post('/users/login', {
                    user: {
                        email: userData.email,
                        password: 'wrong-password'
                    }
                });
                expect.fail('Should have thrown an error for wrong password');
            } catch (error: any) {
                expect(error.status).toBe(401);
                expect(error.data.errors.body).toContain('Invalid email or password');
            }
        });
    });
});
