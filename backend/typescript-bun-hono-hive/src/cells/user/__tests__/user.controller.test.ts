import { describe, it, expect, mock, beforeEach } from "bun:test";
import { UserHandler } from "../user.controller.js";
import { mockTokenGenerator } from "../../../tests/mocks/user.mocks.js";
import { User } from "../user.domain.js";
import { JwtTokenGenerator } from "../../../shared/security/token.js";
import { errorHandler } from "../../../shared/web/error-handler.js";
import { Hono } from "hono";

describe("UserHandler", () => {
    let handler: UserHandler;
    let service: any;
    let tokenGenerator: any;
    let jwtTokenGenerator: any;
    let app: Hono<any>;

    beforeEach(() => {
        service = {
            register: mock(),
            login: mock(),
            getUser: mock(),
            updateUser: mock(),
            getProfile: mock(),
            followUser: mock(),
            unfollowUser: mock(),
        };
        tokenGenerator = mockTokenGenerator();
        jwtTokenGenerator = new JwtTokenGenerator("secret");
        handler = new UserHandler(service, tokenGenerator, jwtTokenGenerator);
        app = handler.getApp();
        app.onError(errorHandler);
    });

    describe("register", () => {
        it("should return 201 and user data on successful registration", async () => {
            const user = new User(1, "testuser", "test@example.com", "hash", "", null);
            service.register.mockResolvedValue(user);
            tokenGenerator.generate.mockResolvedValue("token123");

            const res = await app.request("/users", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    user: {
                        username: "testuser",
                        email: "test@example.com",
                        password: "password123456", // Min 8 chars
                    },
                }),
            });

            expect(res.status).toBe(201);
            expect(await res.json()).toEqual({
                user: {
                    email: user.email,
                    username: user.username,
                    token: "token123",
                    bio: user.bio,
                    image: user.image,
                },
            });
        });

        it("should return 422 if validation fails", async () => {
            const res = await app.request("/users", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    user: {
                        username: "testuser",
                        email: "invalid-email",
                        password: "short",
                    },
                }),
            });

            expect(res.status).toBe(422);
            expect(service.register).not.toHaveBeenCalled();
        });
    });
});
