interface ConduitDebug {
    getToken(): string | null;
    getAuthState(): "authenticated" | "unauthenticated" | "unavailable" | "loading";
    getCurrentUser(): {
        username: string;
        email: string;
        bio: string | null;
        image: string | null;
        token: string;
    } | null;
}

interface Window {
    __conduit_debug__: ConduitDebug;
}
