import { describe, it, expect, beforeAll, afterAll, beforeEach } from "vitest";
import request from "supertest";
import { App } from "../../hive/app.js";

describe("Profile API Integration", () => {
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

    it("should get a profile", async () => {
        // Register user 1
        await request(expressApp)
            .post("/api/users")
            .send({
                user: {
                    username: "user1",
                    email: "user1@example.com",
                    password: "password123",
                },
            });

        const resp = await request(expressApp)
            .get("/api/profiles/user1")
            .expect(200);

        expect(resp.body.profile.username).toBe("user1");
        expect(resp.body.profile.following).toBe(false);
    });

    it("should follow and unfollow a user", async () => {
        // Register user 1
        const reg1 = await request(expressApp)
            .post("/api/users")
            .send({
                user: {
                    username: "user1",
                    email: "user1@example.com",
                    password: "password123",
                },
            });
        const token1 = reg1.body.user.token;

        // Register user 2
        await request(expressApp)
            .post("/api/users")
            .send({
                user: {
                    username: "user2",
                    email: "user2@example.com",
                    password: "password123",
                },
            });

        // user1 follows user2
        const followResp = await request(expressApp)
            .post("/api/profiles/user2/follow")
            .set("Authorization", `Token ${token1}`)
            .expect(200);

        expect(followResp.body.profile.username).toBe("user2");
        expect(followResp.body.profile.following).toBe(true);

        // check profile without auth
        const profileResp = await request(expressApp)
            .get("/api/profiles/user2")
            .expect(200);
        expect(profileResp.body.profile.following).toBe(false);

        // check profile with auth
        const profileAuthResp = await request(expressApp)
            .get("/api/profiles/user2")
            .set("Authorization", `Token ${token1}`)
            .expect(200);
        expect(profileAuthResp.body.profile.following).toBe(true);

        // user1 unfollows user2
        const unfollowResp = await request(expressApp)
            .delete("/api/profiles/user2/follow")
            .set("Authorization", `Token ${token1}`)
            .expect(200);

        expect(unfollowResp.body.profile.following).toBe(false);
    });
});
