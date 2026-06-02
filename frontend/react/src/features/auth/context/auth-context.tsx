import React, { createContext, useContext, useState, useEffect } from "react";
import { AUTH_EVENT } from "@/shared/api/events.ts";

export interface AuthContextType {
    username: string | null;
    token: string | null;
    isAuthenticated: boolean;
    login: (username: string, token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: React.ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [username, setUsername] = useState<string | null>(() => {
        return localStorage.getItem("realworld_username");
    });
    const [token, setToken] = useState<string | null>(() => {
        return localStorage.getItem("jwtToken");
    });

    const login = (newUsername: string, newToken: string) => {
        localStorage.setItem("realworld_username", newUsername);
        localStorage.setItem("jwtToken", newToken);
        setUsername(newUsername);
        setToken(newToken);
    };

    const logout = () => {
        localStorage.removeItem("realworld_username");
        localStorage.removeItem("jwtToken");
        setUsername(null);
        setToken(null);
    };

    // Sync logout/login changes across multiple open browser tabs
    useEffect(() => {
        const handleStorageChange = (e: StorageEvent) => {
            if (e.key === "jwtToken") {
                // If token was deleted or changed in another tab, update state
                setToken(e.newValue);
            }
            if (e.key === "realworld_username") {
                setUsername(e.newValue);
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
        window.__conduit_debug__ = {
            getToken: () => token,
            getAuthState: (): "authenticated" | "unauthenticated" | "unavailable" | "loading" => {
                if (token === null && username === null) {
                    return "unauthenticated";
                }
                if (token) {
                    return "authenticated";
                }
                return "unavailable";
            },
            getCurrentUser: () => {
                if (!token || !username) return null;
                return {
                    username,
                    email: "",
                    bio: null,
                    image: null,
                    token,
                };
            },
        };
    }, [token, username]);

    return (
        <AuthContext.Provider value={{ username, token, isAuthenticated, login, logout }}>
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
