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
 * Generates a full set of unique user data.
 */
export function generateUserData() {
    const id = unique('user');
    return {
        username: id,
        email: `${id}@example.com`,
        password: 'password123'
    };
}
