import { describe, it, expect } from "bun:test";
import { BunPasswordHasher } from "../security/password.js";
import { AppError, ErrorType } from "../errors/app-error.js";

describe("BunPasswordHasher", () => {
    const hasher = new BunPasswordHasher();

    it("should hash and compare passwords correctly", async () => {
        const password = "password123";
        const hash = await hasher.hash(password);

        expect(hash).not.toBe(password);
        await expect(hasher.compare(hash, password)).resolves.toBeUndefined();
    });

    it("should throw error for invalid password", async () => {
        const password = "password123";
        const hash = await hasher.hash(password);

        await expect(hasher.compare(hash, "wrong-password")).rejects.toMatchObject({
            type: ErrorType.InvalidCredentials,
        });
    });
});
