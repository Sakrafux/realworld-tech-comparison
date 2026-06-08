import { sleep } from "k6";
import { randomString, registerUser } from "../utils.ts";

export default function () {
    const username = `user_${randomString(10)}`;
    const email = `${username}@example.com`;
    const password = "password123";

    registerUser(username, email, password);
    sleep(1);
}
