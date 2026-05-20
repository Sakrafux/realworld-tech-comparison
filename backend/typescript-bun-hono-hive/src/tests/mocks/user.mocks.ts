import { mock } from "bun:test";
import type { UserRepository } from "../../cells/user/user.ports.js";
import type { PasswordHasher } from "../../shared/security/password.js";
import type { TokenGenerator } from "../../shared/security/token.js";

export const mockUserRepository = (): UserRepository => ({
    create: mock(),
    findByEmail: mock(),
    findByUsername: mock(),
    findById: mock(),
    update: mock(),
    getProfileByUsername: mock(),
    follow: mock(),
    unfollow: mock(),
});

export const mockPasswordHasher = (): PasswordHasher => ({
    hash: mock(),
    compare: mock(),
});

export const mockTokenGenerator = (): TokenGenerator => ({
    generate: mock(),
});
