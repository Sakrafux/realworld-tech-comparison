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

export type UpdateUserRequest = {
    user: {
        username?: string;
        email?: string;
        password?: string;
        bio?: string;
        image?: string;
    };
};

export type User = {
    email: string;
    token: string;
    username: string;
    bio: string;
    image?: string;
};

export type UserResponse = {
    user: User;
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

export async function updateUser(updateUserRequest: UpdateUserRequest): Promise<UserResponse> {
    return api.put<UserResponse>("/user", updateUserRequest);
}
