import { vi } from "vitest";
import { UserRepository } from "../../cells/user/ports.js";
import { PasswordHasher } from "../../shared/security/password.js";
import { TokenGenerator } from "../../shared/security/token.js";

export const mockUserRepository = (): UserRepository => ({
    create: vi.fn(),
    findByEmail: vi.fn(),
    findByUsername: vi.fn(),
    findById: vi.fn(),
    update: vi.fn(),
    getProfileByUsername: vi.fn(),
    follow: vi.fn(),
    unfollow: vi.fn(),
});

export const mockPasswordHasher = (): PasswordHasher => ({
    hash: vi.fn(),
    compare: vi.fn(),
});

export const mockTokenGenerator = (): TokenGenerator => ({
    generate: vi.fn(),
});
