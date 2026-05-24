import api from "@/shared/api";

export type LoginRequest = {
    user: {
        email: string;
        password: string;
    };
};

export type RegisterRequest = {
    user: {
        username: string;
        email: string;
        password: string;
    };
};

export type UserResponse = {
    user: {
        email: string;
        token: string;
        username: string;
        bio: string;
        image?: string;
    };
};

export async function login(loginRequest: LoginRequest): Promise<UserResponse> {
    return api.post<UserResponse>("/users/login", loginRequest);
}

export async function register(registerRequest: RegisterRequest): Promise<UserResponse> {
    return api.post<UserResponse>("/users", registerRequest);
}

export async function getUser(): Promise<UserResponse> {
    return api.get<UserResponse>("/user");
}
