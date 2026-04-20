import { apiClient } from './apiClient';

/**
 * Utilities for API testing.
 */

/**
 * Generates a unique string using a prefix and current timestamp/random number.
 * Useful for usernames, emails, etc. to avoid collisions in a shared database.
 */
export function unique(prefix: string): string {
    return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
}

/**
 * Generates a unique tag limited to 20 characters.
 */
export function uniqueTag(): string {
    const timestamp = Date.now().toString().slice(-8);
    const random = Math.floor(Math.random() * 100).toString();
    const tag = `tag-${timestamp}-${random}`;
    return tag.slice(0, 20);
}

/**
 * Generates a full set of unique user data (raw object).
 */
export function generateUserData() {
    const id = unique('user');
    return {
        username: id,
        email: `${id}@example.com`,
        password: 'password123'
    };
}

/**
 * Creates a new user via API and returns the user object (including token).
 */
export async function createUserData() {
    const userData = generateUserData();
    const response = await apiClient.post<any>('/users', { user: userData });
    // Merge raw password for convenience in login tests if needed
    return { 
        user: response.data.user,
        password: userData.password 
    };
}

/**
 * Helper to create an article via API.
 */
export async function createArticle(token: string, tagList: string[] = []) {
    const articleData = {
        title: unique('Article'),
        description: 'Test description',
        body: 'Test body',
        tagList
    };
    const response = await apiClient.post<any>('/articles', { article: articleData }, { token });
    return response.data.article;
}
