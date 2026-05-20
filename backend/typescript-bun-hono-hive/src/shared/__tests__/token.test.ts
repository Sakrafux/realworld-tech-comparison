import { describe, it, expect } from "bun:test";
import { JwtTokenGenerator } from "../security/token.js";

describe("JwtTokenGenerator", () => {
    const secret = "super-secret-key-1234567890123456";
    const generator = new JwtTokenGenerator(secret);

    it("should generate and verify tokens correctly", async () => {
        const userId = 1;
        const token = await generator.generate(userId);

        expect(token).toBeDefined();
        const verifiedId = await generator.verify(token);
        expect(verifiedId).toBe(userId);
    });

    it("should throw error for invalid token", async () => {
        await expect(generator.verify("invalid-token")).rejects.toThrow();
    });
});
