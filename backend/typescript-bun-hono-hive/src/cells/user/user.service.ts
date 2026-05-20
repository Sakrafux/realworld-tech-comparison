import { User, type Profile } from "./user.domain.js";
import type {
    RegisterCommand,
    LoginCommand,
    UpdateUserCommand,
    UserService,
    UserRepository,
} from "./user.ports.js";
import {
    newAlreadyExistsError,
    newInvalidCredentialsError,
    newNotFoundError,
    newResourceNotFound,
} from "../../shared/errors/app-error.js";
import type { PasswordHasher } from "../../shared/security/password.js";

export class DefaultUserService implements UserService {
    constructor(
        private repo: UserRepository,
        private hasher: PasswordHasher,
    ) {}

    async register(cmd: RegisterCommand): Promise<User> {
        const existingEmail = await this.repo.findByEmail(cmd.email);
        if (existingEmail) {
            throw newAlreadyExistsError("Email already exists");
        }

        const existingUsername = await this.repo.findByUsername(cmd.username);
        if (existingUsername) {
            throw newAlreadyExistsError("Username already exists");
        }

        const hashedPassword = await this.hasher.hash(cmd.password);
        const user = new User(0, cmd.username, cmd.email, hashedPassword, "", null);

        await this.repo.create(user);
        return user;
    }

    async login(cmd: LoginCommand): Promise<User> {
        const user = await this.repo.findByEmail(cmd.email);
        if (!user) {
            throw newNotFoundError("User not found");
        }

        await this.hasher.compare(user.password, cmd.password);
        return user;
    }

    async getUser(id: number): Promise<User> {
        const user = await this.repo.findById(id);
        if (!user) {
            throw newResourceNotFound("User", "id", id);
        }
        return user;
    }

    async getUserByUsername(username: string): Promise<User> {
        const user = await this.repo.findByUsername(username);
        if (!user) {
            throw newResourceNotFound("User", "username", username);
        }
        return user;
    }

    async updateUser(cmd: UpdateUserCommand): Promise<User> {
        const user = await this.getUser(cmd.id);

        if (cmd.email && cmd.email !== user.email) {
            const existingEmail = await this.repo.findByEmail(cmd.email);
            if (existingEmail) {
                throw newAlreadyExistsError("Email already exists");
            }
        }

        if (cmd.username && cmd.username !== user.username) {
            const existingUsername = await this.repo.findByUsername(cmd.username);
            if (existingUsername) {
                throw newAlreadyExistsError("Username already exists");
            }
        }

        let hashedPassword;
        if (cmd.password) {
            hashedPassword = await this.hasher.hash(cmd.password);
        }

        user.update({
            username: cmd.username,
            email: cmd.email,
            bio: cmd.bio,
            image: cmd.image,
            password: hashedPassword,
        });

        await this.repo.update(user);
        return user;
    }

    async getProfile(username: string, observerId?: number): Promise<Profile> {
        const profile = await this.repo.getProfileByUsername(username, observerId);
        if (!profile) {
            throw newResourceNotFound("Profile", "username", username);
        }
        return profile;
    }

    async followUser(followerId: number, username: string): Promise<Profile> {
        const userToFollow = await this.repo.findByUsername(username);
        if (!userToFollow) {
            throw newResourceNotFound("User", "username", username);
        }

        await this.repo.follow(followerId, userToFollow.id);
        return this.getProfile(username, followerId);
    }

    async unfollowUser(followerId: number, username: string): Promise<Profile> {
        const userToUnfollow = await this.repo.findByUsername(username);
        if (!userToUnfollow) {
            throw newResourceNotFound("User", "username", username);
        }

        await this.repo.unfollow(followerId, userToUnfollow.id);
        return this.getProfile(username, followerId);
    }
}
