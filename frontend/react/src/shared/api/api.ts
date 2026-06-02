import { triggerLogout } from "@/shared/api/events.ts";

const BASE_URL: string = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const AUTH_URLS = ["register", "login"];

function isAuthEndpoint(endpoint: string) {
    return AUTH_URLS.some((authEndpoint) => endpoint.includes(authEndpoint));
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const separator = endpoint.startsWith("/") ? "" : "/";
    const url = `${BASE_URL}${separator}${endpoint}`;

    const headers: Record<string, string> = {
        "Content-Type": "application/json",
        ...(options.headers as Record<string, string>),
    };

    // Safe localStorage read for the browser
    const token = localStorage.getItem("jwtToken");
    if (token) {
        headers["Authorization"] = `Token ${token}`;
    }

    const config: RequestInit = {
        ...options,
        headers,
    };

    const response = await fetch(url, config);

    if (!response.ok) {
        // Can't be successfully authenticated in case of 401
        if (response.status === 401 && !isAuthEndpoint(endpoint)) {
            // Implicitly causes the removal of the localStorage token
            triggerLogout();
        }

        const errorData = await response.json().catch(() => ({}));
        if (errorData?.errors?.body && Array.isArray(errorData.errors.body)) {
            throw new Error(errorData.errors.body.join(" - "));
        }
        throw new Error(`HTTP error! Status: ${response.status}`);
    }

    if (response.status === 204) {
        return null as T;
    }

    return response.json().catch(() => null as T);
}

const api = {
    get: <T>(
        endpoint: string,
        searchParams: Record<string, string> = {},
        options: Omit<RequestInit, "method" | "body"> = {},
    ): Promise<T> => {
        const searchParamsStr = new URLSearchParams(searchParams).toString();
        return request<T>(`${endpoint}${searchParamsStr ? "?" + searchParamsStr : ""}`, {
            ...options,
            method: "GET",
        });
    },

    post: <T>(
        endpoint: string,
        body?: object,
        options: Omit<RequestInit, "method" | "body"> = {},
    ): Promise<T> =>
        request<T>(endpoint, { ...options, method: "POST", body: JSON.stringify(body) }),

    put: <T>(
        endpoint: string,
        body?: object,
        options: Omit<RequestInit, "method" | "body"> = {},
    ): Promise<T> =>
        request<T>(endpoint, { ...options, method: "PUT", body: JSON.stringify(body) }),

    delete: <T>(endpoint: string, options: Omit<RequestInit, "method" | "body"> = {}): Promise<T> =>
        request<T>(endpoint, { ...options, method: "DELETE" }),
};

export default api;
