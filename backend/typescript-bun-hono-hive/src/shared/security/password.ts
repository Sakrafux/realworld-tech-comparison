import { newInvalidCredentialsError } from "../errors/app-error.js";

export interface PasswordHasher {
    hash(password: string): Promise<string>;
    compare(hashedPassword: string, password: string): Promise<void>;
}

export class BunPasswordHasher implements PasswordHasher {
    async hash(password: string): Promise<string> {
        return await Bun.password.hash(password, {
            algorithm: "argon2id",
            memoryCost: 65536,
            timeCost: 3,
        });
    }

    async compare(hashedPassword: string, password: string): Promise<void> {
        const valid = await Bun.password.verify(password, hashedPassword);
        if (!valid) {
            throw newInvalidCredentialsError("Invalid email or password");
        }
    }
}
