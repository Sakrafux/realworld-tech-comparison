import api from "@/shared/api";

export type LoginRequest = {
    user: {
        email: string;
        password: string;
    };
};

export type LoginResponse = {
    user: {
        email: string;
        token: string;
        username: string;
        bio: string;
        image: string;
    };
};

export async function login(loginRequest: LoginRequest): Promise<LoginResponse> {
    return api.post<LoginResponse>("/users/login", loginRequest);
}
