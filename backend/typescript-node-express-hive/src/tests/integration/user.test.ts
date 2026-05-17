import { describe, it, expect, beforeAll, afterAll, beforeEach } from "vitest";
import request from "supertest";
import { App } from "../../hive/app.js";

describe("User API Integration", () => {
    let app: App;
    let expressApp: any;

    beforeAll(async () => {
        app = new App();
        expressApp = await app.bootstrap();
    });

    afterAll(async () => {
        await app.shutdown();
    });

    beforeEach(async () => {
        const db = app.getDatabase();
        await db.none("TRUNCATE app_user RESTART IDENTITY CASCADE");
    });

    it("should register and login a user", async () => {
        const regReq = {
            user: {
                username: "testuser",
                email: "test@example.com",
                password: "password123",
            },
        };

        const regResp = await request(expressApp)
            .post("/api/users")
            .send(regReq)
            .expect(201);

        expect(regResp.body.user.username).toBe("testuser");
        expect(regResp.body.user.email).toBe("test@example.com");
        expect(regResp.body.user.token).toBeDefined();

        const loginReq = {
            user: {
                email: "test@example.com",
                password: "password123",
            },
        };

        const loginResp = await request(expressApp)
            .post("/api/users/login")
            .send(loginReq)
            .expect(200);

        expect(loginResp.body.user.token).toBeDefined();
        expect(loginResp.body.user.username).toBe("testuser");
    });

    it("should get the current user", async () => {
        const regReq = {
            user: {
                username: "testuser",
                email: "test@example.com",
                password: "password123",
            },
        };

        const regResp = await request(expressApp)
            .post("/api/users")
            .send(regReq);

        const token = regResp.body.user.token;

        const getResp = await request(expressApp)
            .get("/api/user")
            .set("Authorization", `Token ${token}`)
            .expect(200);

        expect(getResp.body.user.email).toBe("test@example.com");
    });

    it("should update the current user", async () => {
        const regReq = {
            user: {
                username: "testuser",
                email: "test@example.com",
                password: "password123",
            },
        };

        const regResp = await request(expressApp)
            .post("/api/users")
            .send(regReq);

        const token = regResp.body.user.token;

        const updateReq = {
            user: {
                bio: "I am a developer",
                image: "https://example.com/image.png",
            },
        };

        const updateResp = await request(expressApp)
            .put("/api/user")
            .set("Authorization", `Token ${token}`)
            .send(updateReq)
            .expect(200);

        expect(updateResp.body.user.bio).toBe("I am a developer");
        expect(updateResp.body.user.image).toBe("https://example.com/image.png");
    });

    it("should return 401 if unauthorized", async () => {
        await request(expressApp)
            .get("/api/user")
            .expect(401);
    });
});
