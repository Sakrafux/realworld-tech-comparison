import { Router, type Request, type Response, type NextFunction } from "express";
import type { UserService } from "./ports.js";
import { type AuthenticatedRequest, authMiddleware } from "../../shared/web/auth-middleware.js";
import { JwtTokenGenerator, type TokenGenerator } from "../../shared/security/token.js";
import { loginSchema, registerSchema, updateUserSchema } from "./validator.js";

export class UserHandler {
    constructor(
        private service: UserService,
        private tokenGenerator: TokenGenerator,
        private jwtTokenGenerator: JwtTokenGenerator, // Specific for middleware
    ) {}

    getRouter(): Router {
        const router = Router();

        router.post("/users/login", this.login.bind(this));
        router.post("/users", this.register.bind(this));
        router.get("/user", authMiddleware(this.jwtTokenGenerator), this.getCurrentUser.bind(this));
        router.put(
            "/user",
            authMiddleware(this.jwtTokenGenerator),
            this.updateCurrentUser.bind(this),
        );

        router.get(
            "/profiles/:username",
            authMiddleware(this.jwtTokenGenerator, false),
            this.getProfile.bind(this),
        );
        router.post(
            "/profiles/:username/follow",
            authMiddleware(this.jwtTokenGenerator),
            this.follow.bind(this),
        );
        router.delete(
            "/profiles/:username/follow",
            authMiddleware(this.jwtTokenGenerator),
            this.unfollow.bind(this),
        );

        return router;
    }

    private async register(req: Request, res: Response, next: NextFunction) {
        try {
            const validated = registerSchema.parse(req.body);
            const { user: userIn } = validated;
            const user = await this.service.register({
                username: userIn.username,
                email: userIn.email,
                password: userIn.password,
            });

            const token = await this.tokenGenerator.generate(user.id);
            res.status(201).json({ user: this.mapUserToResponse(user, token) });
        } catch (err) {
            next(err);
        }
    }

    private async login(req: Request, res: Response, next: NextFunction) {
        try {
            const validated = loginSchema.parse(req.body);
            const { user: userIn } = validated;
            const user = await this.service.login({
                email: userIn.email,
                password: userIn.password,
            });

            const token = await this.tokenGenerator.generate(user.id);
            res.json({ user: this.mapUserToResponse(user, token) });
        } catch (err) {
            next(err);
        }
    }

    private async getCurrentUser(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const user = await this.service.getUser(req.userId!);
            const authHeader = req.headers.authorization!;
            const token = authHeader.split(" ")[1]!;
            res.json({ user: this.mapUserToResponse(user, token) });
        } catch (err) {
            next(err);
        }
    }

    private async updateCurrentUser(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const validated = updateUserSchema.parse(req.body);
            const { user: userIn } = validated;
            const user = await this.service.updateUser({
                id: req.userId!,
                username: userIn.username,
                email: userIn.email,
                password: userIn.password,
                bio: userIn.bio,
                image: userIn.image,
            });

            const authHeader = req.headers.authorization!;
            const token = authHeader.split(" ")[1]!;
            res.json({ user: this.mapUserToResponse(user, token) });
        } catch (err) {
            next(err);
        }
    }

    private async getProfile(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const profile = await this.service.getProfile(
                req.params.username as string,
                req.userId,
            );
            res.json({ profile });
        } catch (err) {
            next(err);
        }
    }

    private async follow(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const profile = await this.service.followUser(
                req.userId!,
                req.params.username as string,
            );
            res.json({ profile });
        } catch (err) {
            next(err);
        }
    }

    private async unfollow(req: AuthenticatedRequest, res: Response, next: NextFunction) {
        try {
            const profile = await this.service.unfollowUser(
                req.userId!,
                req.params.username as string,
            );
            res.json({ profile });
        } catch (err) {
            next(err);
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
