import type { NextFunction, Request, Response } from "express";
import { JwtTokenGenerator } from "../security/token.js";
import { newUnauthorizedError } from "../errors/app-error.js";

export interface AuthenticatedRequest extends Request {
    userId?: number;
}

export function authMiddleware(tokenGenerator: JwtTokenGenerator, required: boolean = true) {
    return async (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
        const authHeader = req.headers.authorization;
        if (!authHeader) {
            if (required) {
                return next(newUnauthorizedError("Authorization header is required"));
            }
            return next();
        }

        const parts = authHeader.split(" ");
        if (parts.length !== 2 || parts[0] !== "Token") {
            if (required) {
                return next(newUnauthorizedError("Invalid authorization header format"));
            }
            return next();
        }

        const token = parts[1];
        if (!token) {
            if (required) {
                return next(newUnauthorizedError("Token is required"));
            }
            return next();
        }

        try {
            req.userId = await tokenGenerator.verify(token);
            next();
        } catch (err) {
            if (required) {
                return next(newUnauthorizedError("Invalid or expired token"));
            }
            next();
        }
    };
}
