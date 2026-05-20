import { describe, it, expect } from "bun:test";
import { AppError, ErrorType, newNotFoundError, newResourceNotFound } from "../errors/app-error.js";

describe("AppError", () => {
    it("should create an error with the correct type and message", () => {
        const error = new AppError(ErrorType.NotFound, "Not found");
        expect(error.type).toBe(ErrorType.NotFound);
        expect(error.message).toBe("Not found");
    });

    it("should create a Not Found error using the helper", () => {
        const error = newNotFoundError("User not found");
        expect(error.type).toBe(ErrorType.NotFound);
        expect(error.message).toBe("User not found");
    });

    it("should create a Resource Not Found error using the helper", () => {
        const error = newResourceNotFound("User", "id", 1);
        expect(error.type).toBe(ErrorType.NotFound);
        expect(error.message).toBe("User not found with id: '1'");
    });
});
