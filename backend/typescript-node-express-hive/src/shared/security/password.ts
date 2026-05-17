import * as argon2 from "argon2";
import { newInvalidCredentialsError } from "../errors/app-error.js";

export interface PasswordHasher {
    hash(password: string): Promise<string>;
    compare(hashedPassword: string, password: string): Promise<void>;
}

export class Argon2PasswordHasher implements PasswordHasher {
    async hash(password: string): Promise<string> {
        return await argon2.hash(password);
    }

    async compare(hashedPassword: string, password: string): Promise<void> {
        const valid = await argon2.verify(hashedPassword, password);
        if (!valid) {
            throw newInvalidCredentialsError("Invalid email or password");
        }
    }
}
