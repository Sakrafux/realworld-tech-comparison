import { ZodError } from "zod";
import { AppError, ErrorType } from "../errors/app-error.js";
import type { Context } from "hono";

export function errorHandler(err: Error, c: Context): Response {
    if (err instanceof AppError) {
        switch (err.type) {
            case ErrorType.NotFound:
                return respond(c, 404, err.message);
            case ErrorType.AlreadyExists:
                return respond(c, 422, err.message);
            case ErrorType.InvalidCredentials:
            case ErrorType.Unauthorized:
                return respond(c, 401, err.message);
            case ErrorType.Forbidden:
                return respond(c, 403, err.message);
            case ErrorType.Unprocessable:
                return respond(c, 422, err.message);
            case ErrorType.Internal:
                console.error(err.message);
                return respond(c, 500, err.message);
            default:
                return respond(c, 500, "An unexpected error occurred");
        }
    }

    if (err instanceof ZodError) {
        const messages = err.issues.map((e) => `${e.path.join(".")} ${e.message}`);
        return respondMultiple(c, 422, messages);
    }

    // Generic fallback for other errors
    console.error(err);
    const message = err.message || "Internal Server Error";
    return respond(c, 500, message);
}

export function respond(c: Context, code: number, message: string): Response {
    return respondMultiple(c, code, [message]);
}

export function respondMultiple(c: Context, code: number, messages: string[]): Response {
    return c.json({
        errors: {
            body: messages,
        },
    }, code as any);
}
