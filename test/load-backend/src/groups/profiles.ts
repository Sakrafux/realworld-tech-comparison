import http from "k6/http";
import { check, group } from "k6";
import { BASE_URL, randomItem, User } from "../utils.ts";

export function profiles(authParams: any, users: User[], currentUser: User) {
    group("Profiles", () => {
        const targetUser = randomItem(users);

        // Don't follow yourself
        if (targetUser.username === currentUser.username) {
            return;
        }

        let res = http.get(`${BASE_URL}/profiles/${targetUser.username}`, {
            ...authParams,
            tags: { name: "GetProfile" },
        });
        check(res, { "get profile status 200": (r) => r.status === 200 });

        res = http.post(`${BASE_URL}/profiles/${targetUser.username}/follow`, null, {
            ...authParams,
            tags: { name: "FollowUser" },
        });
        check(res, { "follow user status 200": (r) => r.status === 200 });

        res = http.del(`${BASE_URL}/profiles/${targetUser.username}/follow`, null, {
            ...authParams,
            tags: { name: "UnfollowUser" },
        });
        check(res, { "unfollow user status 200": (r) => r.status === 200 });
    });
}
