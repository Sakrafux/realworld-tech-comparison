import { describe, it, expect } from "vitest";
import request from "supertest";
import app from "./app.js";

describe("GET /", () => {
    it("should return a 200 status and the welcome message", async () => {
        const response = await request(app).get("/");

        expect(response.status).toBe(200);
        expect(response.body).toEqual({
            message: "Hello World from Express, TypeScript, and pnpm!",
        });
    });
});
