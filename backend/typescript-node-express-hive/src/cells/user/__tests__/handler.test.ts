import { describe, it, expect, beforeEach, vi } from "vitest";
import { UserHandler } from "../handler.js";
import { mockUserRepository, mockTokenGenerator } from "../../../tests/mocks/user.mocks.js";
import { User } from "../user.js";
import { JwtTokenGenerator } from "../../../shared/security/token.js";

describe("UserHandler", () => {
    let handler: UserHandler;
    let service: any;
    let tokenGenerator: any;
    let jwtTokenGenerator: any;

    beforeEach(() => {
        service = {
            register: vi.fn(),
            login: vi.fn(),
            getUser: vi.fn(),
            updateUser: vi.fn(),
            getProfile: vi.fn(),
            followUser: vi.fn(),
            unfollowUser: vi.fn(),
        };
        tokenGenerator = mockTokenGenerator();
        jwtTokenGenerator = new JwtTokenGenerator("secret");
        handler = new UserHandler(service, tokenGenerator, jwtTokenGenerator);
    });

    describe("register", () => {
        it("should return 201 and user data on successful registration", async () => {
            const req: any = {
                body: {
                    user: {
                        username: "testuser",
                        email: "test@example.com",
                        password: "password123",
                    },
                },
            };
            const res: any = {
                status: vi.fn().mockReturnThis(),
                json: vi.fn(),
            };
            const next = vi.fn();

            const user = new User(1, "testuser", "test@example.com", "hash", "", null);
            service.register.mockResolvedValue(user);
            tokenGenerator.generate.mockResolvedValue("token123");

            await (handler as any).register(req, res, next);

            expect(res.status).toHaveBeenCalledWith(201);
            expect(res.json).toHaveBeenCalledWith({
                user: {
                    email: user.email,
                    username: user.username,
                    token: "token123",
                    bio: user.bio,
                    image: user.image,
                },
            });
        });

        it("should call next with error if registration fails", async () => {
            const req: any = {
                body: {
                    user: {
                        username: "testuser",
                        email: "invalid-email",
                        password: "short",
                    },
                },
            };
            const res: any = {
                status: vi.fn().mockReturnThis(),
                json: vi.fn(),
            };
            const next = vi.fn();

            // Validation will throw before calling service
            await (handler as any).register(req, res, next);

            expect(next).toHaveBeenCalled();
            expect(service.register).not.toHaveBeenCalled();
        });
    });
});
