import http from "k6/http";
import { check, group } from "k6";
import { BASE_URL } from "../utils.ts";

export function loginAndCheck(email: string, password: string) {
    group("Login and Check", () => {
        const loginPayload = JSON.stringify({
            user: { email, password },
        });
        let res = http.post(`${BASE_URL}/users/login`, loginPayload, {
            headers: { "Content-Type": "application/json" },
            tags: { name: "Login" },
        });
        check(res, { "login status 200": (r) => r.status === 200 });

        if (res.status === 200) {
            const body = res.json() as any;
            const token = body.user.token;

            res = http.get(`${BASE_URL}/user`, {
                headers: { "Content-Type": "application/json", Authorization: `Token ${token}` },
                tags: { name: "GetCurrentUser" },
            });
            check(res, { "get current user status 200": (r) => r.status === 200 });
        }
    });
}
