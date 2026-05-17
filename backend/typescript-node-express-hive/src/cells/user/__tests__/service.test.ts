import { describe, it, expect, beforeEach, vi } from "vitest";
import { DefaultUserService } from "../service.js";
import { User } from "../user.js";
import { mockUserRepository, mockPasswordHasher } from "../../../tests/mocks/user.mocks.js";
import { ErrorType } from "../../../shared/errors/app-error.js";

describe("DefaultUserService", () => {
    let service: DefaultUserService;
    let repo: any;
    let hasher: any;

    beforeEach(() => {
        repo = mockUserRepository();
        hasher = mockPasswordHasher();
        service = new DefaultUserService(repo, hasher);
    });

    describe("register", () => {
        it("should register a new user successfully", async () => {
            const cmd = {
                username: "testuser",
                email: "test@example.com",
                password: "password123",
            };
            repo.findByEmail.mockResolvedValue(null);
            repo.findByUsername.mockResolvedValue(null);
            hasher.hash.mockResolvedValue("hashed-password");
            repo.create.mockImplementation(async (u: User) => {
                u.id = 1;
            });

            const user = await service.register(cmd);

            expect(user.username).toBe(cmd.username);
            expect(user.email).toBe(cmd.email);
            expect(user.password).toBe("hashed-password");
            expect(repo.create).toHaveBeenCalled();
        });

        it("should throw error if email already exists", async () => {
            const cmd = {
                username: "testuser",
                email: "test@example.com",
                password: "password123",
            };
            repo.findByEmail.mockResolvedValue(new User(1, "other", cmd.email, "hash", "", null));

            await expect(service.register(cmd)).rejects.toMatchObject({
                type: ErrorType.AlreadyExists,
                message: "Email already exists",
            });
        });

        it("should throw error if username already exists", async () => {
            const cmd = {
                username: "testuser",
                email: "test@example.com",
                password: "password123",
            };
            repo.findByEmail.mockResolvedValue(null);
            repo.findByUsername.mockResolvedValue(
                new User(1, cmd.username, "other@example.com", "hash", "", null),
            );

            await expect(service.register(cmd)).rejects.toMatchObject({
                type: ErrorType.AlreadyExists,
                message: "Username already exists",
            });
        });
    });

    describe("login", () => {
        it("should return user if credentials are valid", async () => {
            const cmd = { email: "test@example.com", password: "password123" };
            const existingUser = new User(1, "testuser", cmd.email, "hashed-password", "", null);
            repo.findByEmail.mockResolvedValue(existingUser);
            hasher.compare.mockResolvedValue(undefined);

            const user = await service.login(cmd);

            expect(user).toBe(existingUser);
            expect(hasher.compare).toHaveBeenCalledWith("hashed-password", cmd.password);
        });

        it("should throw error if user not found", async () => {
            const cmd = { email: "test@example.com", password: "password123" };
            repo.findByEmail.mockResolvedValue(null);

            await expect(service.login(cmd)).rejects.toMatchObject({
                type: ErrorType.NotFound,
            });
        });
    });
});
