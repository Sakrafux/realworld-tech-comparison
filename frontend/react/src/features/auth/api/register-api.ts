import api from "@/shared/api";

export type RegisterRequest = {
    user: {
        username: string;
        email: string;
        password: string;
    };
};

export type RegisterResponse = {
    user: {
        email: string;
        token: string;
        username: string;
        bio: string;
        image: string;
    };
};

export async function register(registerRequest: RegisterRequest): Promise<RegisterResponse> {
    return api.post<RegisterResponse>("/users", registerRequest);
}
