import { Hono } from "hono";
import type { Context } from "hono";
import type { UserService } from "./user.ports.js";
import { authMiddleware } from "../../shared/web/auth-middleware.js";
import { JwtTokenGenerator, type TokenGenerator } from "../../shared/security/token.js";
import { loginSchema, registerSchema, updateUserSchema } from "./user.validator.js";

type Variables = {
    userId?: number;
};

export class UserHandler {
    constructor(
        private service: UserService,
        private tokenGenerator: TokenGenerator,
        private jwtTokenGenerator: JwtTokenGenerator, // Specific for middleware
    ) {}

    getApp(): Hono<{ Variables: Variables }> {
        const app = new Hono<{ Variables: Variables }>();

        app.post("/users/login", this.login.bind(this));
        app.post("/users", this.register.bind(this));
        app.get("/user", authMiddleware(this.jwtTokenGenerator), this.getCurrentUser.bind(this));
        app.put(
            "/user",
            authMiddleware(this.jwtTokenGenerator),
            this.updateCurrentUser.bind(this),
        );

        app.get(
            "/profiles/:username",
            authMiddleware(this.jwtTokenGenerator, false),
            this.getProfile.bind(this),
        );
        app.post(
            "/profiles/:username/follow",
            authMiddleware(this.jwtTokenGenerator),
            this.follow.bind(this),
        );
        app.delete(
            "/profiles/:username/follow",
            authMiddleware(this.jwtTokenGenerator),
            this.unfollow.bind(this),
        );

        return app;
    }

    private async register(c: Context<{ Variables: Variables }>) {
        try {
            const body = await c.req.json();
            const validated = registerSchema.parse(body);
            const { user: userIn } = validated;
            const user = await this.service.register({
                username: userIn.username,
                email: userIn.email,
                password: userIn.password,
            });

            const token = await this.tokenGenerator.generate(user.id);
            return c.json({ user: this.mapUserToResponse(user, token) }, 201);
        } catch (err) {
            throw err;
        }
    }

    private async login(c: Context<{ Variables: Variables }>) {
        try {
            const body = await c.req.json();
            const validated = loginSchema.parse(body);
            const { user: userIn } = validated;
            const user = await this.service.login({
                email: userIn.email,
                password: userIn.password,
            });

            const token = await this.tokenGenerator.generate(user.id);
            return c.json({ user: this.mapUserToResponse(user, token) });
        } catch (err) {
            throw err;
        }
    }

    private async getCurrentUser(c: Context<{ Variables: Variables }>) {
        try {
            const userId = c.get("userId");
            const user = await this.service.getUser(userId!);
            const authHeader = c.req.header("Authorization")!;
            const token = authHeader.split(" ")[1]!;
            return c.json({ user: this.mapUserToResponse(user, token) });
        } catch (err) {
            throw err;
        }
    }

    private async updateCurrentUser(c: Context<{ Variables: Variables }>) {
        try {
            const body = await c.req.json();
            const validated = updateUserSchema.parse(body);
            const { user: userIn } = validated;
            const userId = c.get("userId");
            const user = await this.service.updateUser({
                id: userId!,
                username: userIn.username,
                email: userIn.email,
                password: userIn.password,
                bio: userIn.bio,
                image: userIn.image,
            });

            const authHeader = c.req.header("Authorization")!;
            const token = authHeader.split(" ")[1]!;
            return c.json({ user: this.mapUserToResponse(user, token) });
        } catch (err) {
            throw err;
        }
    }

    private async getProfile(c: Context<{ Variables: Variables }>) {
        try {
            const username = c.req.param("username")!;
            const userId = c.get("userId");
            const profile = await this.service.getProfile(
                username,
                userId,
            );
            return c.json({ profile });
        } catch (err) {
            throw err;
        }
    }

    private async follow(c: Context<{ Variables: Variables }>) {
        try {
            const username = c.req.param("username")!;
            const userId = c.get("userId");
            const profile = await this.service.followUser(
                userId!,
                username,
            );
            return c.json({ profile });
        } catch (err) {
            throw err;
        }
    }

    private async unfollow(c: Context<{ Variables: Variables }>) {
        try {
            const username = c.req.param("username")!;
            const userId = c.get("userId");
            const profile = await this.service.unfollowUser(
                userId!,
                username,
            );
            return c.json({ profile });
        } catch (err) {
            throw err;
        }
    }

    private mapUserToResponse(user: any, token: string) {
        return {
            email: user.email,
            token: token,
            username: user.username,
            bio: user.bio,
            image: user.image,
        };
    }
}
