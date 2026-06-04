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
