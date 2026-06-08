import http from "k6/http";

export const BASE_URL = __ENV.BASE_URL || "http://localhost:8080/api";

export function randomString(length: number): string {
    const chars = "abcdefghijklmnopqrstuvwxyz0123456789";
    let result = "";
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}

export function randomItem<T>(items: T[]): T {
    return items[Math.floor(Math.random() * items.length)];
}

export function getAuthHeaders(token: string) {
    return {
        headers: {
            "Content-Type": "application/json",
            Authorization: `Token ${token}`,
        },
    };
}

export interface User {
    username: string;
    email: string;
    token: string;
}

export function registerUser(username: string, email: string, password: string): User | null {
    const payload = JSON.stringify({
        user: { username, email, password },
    });

    let res = http.post(`${BASE_URL}/users`, payload, {
        headers: { "Content-Type": "application/json" },
        tags: { name: "Register" },
    });

    if (res.status === 201) {
        const body = res.json() as any;
        return {
            username,
            email,
            token: body.user.token,
        };
    }
    return null;
}

export function setupUsers(count = 10): User[] {
    const users: User[] = [];
    for (let i = 0; i < count; i++) {
        const username = `perf_user_${randomString(5)}_${i}`;
        const email = `${username}@example.com`;
        const password = "password123";

        let user = registerUser(username, email, password);

        if (!user) {
            // Try login if registration failed (user might exist)
            const res = http.post(
                `${BASE_URL}/users/login`,
                JSON.stringify({
                    user: { email, password },
                }),
                {
                    headers: { "Content-Type": "application/json" },
                    tags: { name: "Login" },
                },
            );

            if (res.status === 200) {
                const body = res.json() as any;
                user = {
                    username,
                    email,
                    token: body.user.token,
                };
            }
        }

        if (user) {
            users.push(user);
        }
    }
    return users;
}

export function distributeVUs(
    totalVUs: number,
    weights: { [key: string]: number },
): { [key: string]: number } {
    const totalWeight = Object.values(weights).reduce((a, b) => a + b, 0);
    const distribution: { [key: string]: number } = {};
    let allocated = 0;

    const keys = Object.keys(weights);
    keys.forEach((key, index) => {
        if (index === keys.length - 1) {
            distribution[key] = Math.max(1, totalVUs - allocated);
        } else {
            const share = Math.max(1, Math.floor((weights[key] / totalWeight) * totalVUs));
            distribution[key] = share;
            allocated += share;
        }
    });

    return distribution;
}
