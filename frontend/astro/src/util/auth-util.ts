import type { User } from "@/api/features/user-api.ts";

export function isAuthenticated(): boolean {
    return localStorage.getItem("jwtToken") !== null;
}

export function getCurrentUser(): User | null {
    const serializedUser = localStorage.getItem("realworld_user");
    return serializedUser ? JSON.parse(serializedUser) : null;
}
