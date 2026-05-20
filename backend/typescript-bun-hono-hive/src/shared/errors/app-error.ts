export enum ErrorType {
    NotFound = "NOT_FOUND",
    AlreadyExists = "ALREADY_EXISTS",
    InvalidCredentials = "INVALID_CREDENTIALS",
    Unauthorized = "UNAUTHORIZED",
    Forbidden = "FORBIDDEN",
    Internal = "INTERNAL",
    Unprocessable = "UNPROCESSABLE",
}

export class AppError extends Error {
    constructor(
        public type: ErrorType,
        public message: string,
    ) {
        super(message);
        this.name = "AppError";
    }
}

export const newNotFoundError = (message: string) => new AppError(ErrorType.NotFound, message);
export const newAlreadyExistsError = (message: string) =>
    new AppError(ErrorType.AlreadyExists, message);
export const newInvalidCredentialsError = (message: string) =>
    new AppError(ErrorType.InvalidCredentials, message);
export const newUnauthorizedError = (message: string) =>
    new AppError(ErrorType.Unauthorized, message);
export const newForbiddenError = (message: string) => new AppError(ErrorType.Forbidden, message);
export const newUnprocessableEntityError = (message: string) =>
    new AppError(ErrorType.Unprocessable, message);
export const newInternalError = (message: string) => new AppError(ErrorType.Internal, message);

export const newResourceNotFound = (resource: string, field: string, value: any) =>
    newNotFoundError(`${resource} not found with ${field}: '${value}'`);
