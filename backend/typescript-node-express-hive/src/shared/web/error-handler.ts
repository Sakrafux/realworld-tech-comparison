import type { Request, Response, NextFunction } from "express";
import { ZodError } from "zod";
import { AppError, ErrorType } from "../errors/app-error.js";

export function errorHandler(err: any, req: Request, res: Response, next: NextFunction) {
    if (err instanceof AppError) {
        switch (err.type) {
            case ErrorType.NotFound:
                return respond(res, 404, err.message);
            case ErrorType.AlreadyExists:
                return respond(res, 422, err.message);
            case ErrorType.InvalidCredentials:
            case ErrorType.Unauthorized:
                return respond(res, 401, err.message);
            case ErrorType.Forbidden:
                return respond(res, 403, err.message);
            case ErrorType.Unprocessable:
                return respond(res, 422, err.message);
            case ErrorType.Internal:
                console.error(err.message);
                return respond(res, 500, err.message);
            default:
                return respond(res, 500, "An unexpected error occurred");
        }
    }

    if (err instanceof ZodError) {
        const messages = err.issues.map((e) => `${e.path.join(".")} ${e.message}`);
        return respondMultiple(res, 422, messages);
    }

    // Generic fallback for other errors
    console.error(err);
    const message = err instanceof Error ? err.message : "Internal Server Error";
    respond(res, 500, message);
}

export function respond(res: Response, code: number, message: string) {
    respondMultiple(res, code, [message]);
}

export function respondMultiple(res: Response, code: number, messages: string[]) {
    res.status(code).json({
        errors: {
            body: messages,
        },
    });
}
