import type { Context, Next } from "hono";
import type { JwtTokenGenerator } from "../security/token.js";
import { newUnauthorizedError } from "../errors/app-error.js";

export function authMiddleware(tokenGenerator: JwtTokenGenerator, required: boolean = true) {
    return async (c: Context, next: Next) => {
        const authHeader = c.req.header("Authorization");
        if (!authHeader) {
            if (required) {
                throw newUnauthorizedError("Authorization header is required");
            }
            return await next();
        }

        const parts = authHeader.split(" ");
        if (parts.length !== 2 || parts[0] !== "Token") {
            if (required) {
                throw newUnauthorizedError("Invalid authorization header format");
            }
            return await next();
        }

        const token = parts[1];
        if (!token) {
            if (required) {
                throw newUnauthorizedError("Token is required");
            }
            return await next();
        }

        try {
            const userId = await tokenGenerator.verify(token);
            c.set("userId", userId);
            await next();
        } catch (err) {
            if (required) {
                throw newUnauthorizedError("Invalid or expired token");
            }
            await next();
        }
    };
}
