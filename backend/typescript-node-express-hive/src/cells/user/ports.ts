import { User, type Profile } from "./user.js";

export interface RegisterCommand {
    username: string;
    email: string;
    password: string;
}

export interface LoginCommand {
    email: string;
    password: string;
}

export interface UpdateUserCommand {
    id: number;
    username?: string;
    email?: string;
    password?: string;
    bio?: string;
    image?: string | null;
}

export interface UserService {
    register(cmd: RegisterCommand): Promise<User>;
    login(cmd: LoginCommand): Promise<User>;
    getUser(id: number): Promise<User>;
    getUserByUsername(username: string): Promise<User>;
    updateUser(cmd: UpdateUserCommand): Promise<User>;

    getProfile(username: string, observerId?: number): Promise<Profile>;
    followUser(followerId: number, username: string): Promise<Profile>;
    unfollowUser(followerId: number, username: string): Promise<Profile>;
}

export interface UserRepository {
    create(user: User): Promise<void>;
    findByEmail(email: string): Promise<User | null>;
    findByUsername(username: string): Promise<User | null>;
    findById(id: number): Promise<User | null>;
    update(user: User): Promise<void>;

    getProfileByUsername(username: string, observerId?: number): Promise<Profile | null>;
    follow(followerId: number, followedId: number): Promise<void>;
    unfollow(followerId: number, followedId: number): Promise<void>;
}
