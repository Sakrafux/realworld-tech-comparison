import React, { createContext, useContext, useState, useEffect } from "react";
import { AUTH_EVENT } from "@/shared/api/events.ts";
import type { User } from "@/shared/api/features/user-api.ts";

export interface AuthContextType {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    login: (newUser: User) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: React.ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [user, setUser] = useState<User | null>(() => {
        const serializedUser = localStorage.getItem("realworld_user");
        return serializedUser ? JSON.parse(serializedUser) : null;
    });
    const [token, setToken] = useState<string | null>(() => {
        return localStorage.getItem("jwtToken");
    });

    const login = (newUser: User) => {
        localStorage.setItem("realworld_user", JSON.stringify(newUser));
        localStorage.setItem("jwtToken", newUser.token);
        setUser(newUser);
        setToken(newUser.token);
    };

    const logout = () => {
        localStorage.removeItem("realworld_user");
        localStorage.removeItem("jwtToken");
        setUser(null);
        setToken(null);
    };

    // Sync logout/login changes across multiple open browser tabs
    useEffect(() => {
        const handleStorageChange = (e: StorageEvent) => {
            if (e.key === "jwtToken") {
                // If token was deleted or changed in another tab, update state
                setToken(e.newValue);
            }
            if (e.key === "realworld_user") {
                setUser(e.newValue ? JSON.parse(e.newValue) : null);
            }
        };
        window.addEventListener("storage", handleStorageChange);

        const handleUnauthorized = async () => {
            logout();
            window.location.href = "/login";
        };
        window.addEventListener(AUTH_EVENT, handleUnauthorized);

        return () => {
            window.removeEventListener("storage", handleStorageChange);
            window.removeEventListener(AUTH_EVENT, handleUnauthorized);
        };
    }, []);

    // Derived state for quick checking
    const isAuthenticated = !!token;

    // Expose debug interface on window
    useEffect(() => {
        console.log(user);
        window.__conduit_debug__ = {
            getToken: () => token,
            getAuthState: (): "authenticated" | "unauthenticated" | "unavailable" | "loading" => {
                if (token === null && user === null) {
                    return "unauthenticated";
                }
                if (token) {
                    return "authenticated";
                }
                return "unavailable";
            },
            getCurrentUser: () => {
                if (!token || !user) return null;
                return {
                    username: user.username,
                    email: user.email,
                    bio: user.bio || null,
                    image: user.image || null,
                    token: user.token,
                };
            },
        };
    }, [token, user]);

    return (
        <AuthContext.Provider value={{ user, token, isAuthenticated, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
};
