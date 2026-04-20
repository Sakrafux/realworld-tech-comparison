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
            await apiClient.post('/users', { user: userData });

            try {
                await apiClient.post('/users', {
                    user: {
                        username: 'different-user-' + Date.now(),
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

        it('should return 422 for duplicate username', async () => {
            const userData = generateUserData();
            await apiClient.post('/users', { user: userData });

            try {
                await apiClient.post('/users', {
                    user: {
                        username: userData.username,
                        email: 'different-' + userData.email,
                        password: 'password123'
                    }
                });
                expect.fail('Should have thrown an error for duplicate username');
            } catch (error: any) {
                expect(error.status).toBe(422);
                expect(error.data.errors.body).toContain('Username already exists');
            }
        });

        describe('Validation Errors', () => {
            const testValidation = async (payload: any, expectedErrorSnippet: string) => {
                try {
                    await apiClient.post('/users', payload);
                    expect.fail(`Should have failed for payload: ${JSON.stringify(payload)}`);
                } catch (error: any) {
                    expect(error.status).toBe(422);
                    const allErrors = error.data.errors.body.join(' ');
                    expect(allErrors.toLowerCase()).toContain(expectedErrorSnippet.toLowerCase());
                }
            };

            it('should fail for blank username', () => 
                testValidation({ user: { username: '', email: 'test@example.com', password: 'password123' } }, 'username')
            );

            it('should fail for invalid email', () => 
                testValidation({ user: { username: 'user', email: 'not-an-email', password: 'password123' } }, 'email')
            );

            it('should fail for short password', () => 
                testValidation({ user: { username: 'user', email: 'test@example.com', password: 'short' } }, 'password')
            );

            it('should fail for missing fields', () => 
                testValidation({ user: { username: 'user' } }, 'email')
            );
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

        it('should return 422 for invalid login payload', async () => {
            try {
                await apiClient.post('/users/login', {
                    user: {
                        email: 'not-an-email',
                        password: ''
                    }
                });
                expect.fail('Should have thrown 422');
            } catch (error: any) {
                expect(error.status).toBe(422);
            }
        });
    });
});
