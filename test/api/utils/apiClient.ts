/**
 * Configuration for the API client.
 * Defaults to http://localhost:8080/api if API_BASE_URL is not set in the environment.
 */
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api';

/**
 * Options for API requests, extending standard fetch RequestInit.
 */
export interface ApiRequestOptions extends RequestInit {
    /** Query parameters to be appended to the URL */
    params?: Record<string, string>;
    /** JWT token for Authorization header (prepended with "Token ") */
    token?: string;
}

/**
 * Standard structure for API responses.
 */
export interface ApiResponse<T> {
    /** The parsed JSON body or raw text */
    data: T;
    /** HTTP status code */
    status: number;
    /** Response headers */
    headers: Headers;
}

/**
 * Custom error class for API failures (non-2xx responses).
 */
export class ApiError extends Error {
    /**
     * @param status HTTP status code
     * @param statusText HTTP status text
     * @param data The error payload returned by the API
     */
    constructor(public status: number, public statusText: string, public data: any) {
        super(`API Error: ${status} ${statusText}`);
        this.name = 'ApiError';
    }
}

/**
 * Core request wrapper around native fetch.
 *
 * @param endpoint The API endpoint (e.g., '/users' or 'users')
 * @param options Request options including method, body, params, and token
 * @throws {ApiError} If the response is not ok (status outside 200-299)
 * @returns Promise resolving to a structured ApiResponse
 */
async function request<T>(endpoint: string, options: ApiRequestOptions = {}): Promise<ApiResponse<T>> {
    const { params, token, headers: customHeaders, ...rest } = options;

    // Ensure the URL is constructed correctly relative to the base URL
    const url = new URL(endpoint.startsWith('/') ? endpoint.slice(1) : endpoint, `${API_BASE_URL}/`);

    if (params) {
        Object.entries(params).forEach(([key, value]) => {
            url.searchParams.append(key, value);
        });
    }

    const headers = new Headers(customHeaders);
    headers.set('Content-Type', 'application/json');
    headers.set('Accept', 'application/json');

    if (token) {
        headers.set('Authorization', `Token ${token}`);
    }

    const response = await fetch(url.toString(), {
        ...rest,
        headers,
    });

    let data: any = null;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        data = await response.json();
    } else {
        data = await response.text();
    }

    if (!response.ok) {
        throw new ApiError(response.status, response.statusText, data);
    }

    return {
        data: data as T,
        status: response.status,
        headers: response.headers,
    };
}

/**
 * Strictly typed API client wrapper for common HTTP methods.
 */
export const apiClient = {
    /** Performs a GET request */
    get: <T>(endpoint: string, options?: ApiRequestOptions) =>
        request<T>(endpoint, { ...options, method: 'GET' }),

    /** Performs a POST request with a JSON body */
    post: <T>(endpoint: string, body?: any, options?: ApiRequestOptions) =>
        request<T>(endpoint, { ...options, method: 'POST', body: JSON.stringify(body) }),

    /** Performs a PUT request with a JSON body */
    put: <T>(endpoint: string, body?: any, options?: ApiRequestOptions) =>
        request<T>(endpoint, { ...options, method: 'PUT', body: JSON.stringify(body) }),

    /** Performs a DELETE request */
    delete: <T>(endpoint: string, options?: ApiRequestOptions) =>
        request<T>(endpoint, { ...options, method: 'DELETE' }),
};
