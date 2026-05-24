const BASE_URL: string = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const separator = endpoint.startsWith("/") ? "" : "/";
    const url = `${BASE_URL}${separator}${endpoint}`;

    const headers: Record<string, string> = {
        "Content-Type": "application/json",
        ...(options.headers as Record<string, string>),
    };

    // Safe localStorage read for the browser
    const token = localStorage.getItem("realworld_token");
    if (token) {
        headers["Authorization"] = `Token ${token}`;
    }

    const config: RequestInit = {
        ...options,
        headers,
    };

    const response = await fetch(url, config);

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
    }

    if (response.status === 204) {
        return null as T;
    }

    return response.json();
}

const api = {
    get: <T>(endpoint: string, options: Omit<RequestInit, "method"> = {}): Promise<T> =>
        request<T>(endpoint, { ...options, method: "GET" }),

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

    delete: <T>(endpoint: string, options: Omit<RequestInit, "method"> = {}): Promise<T> =>
        request<T>(endpoint, { ...options, method: "DELETE" }),
};

export default api;
