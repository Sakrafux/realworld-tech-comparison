import * as jose from "jose";

export interface TokenGenerator {
    generate(userId: number): Promise<string>;
}

export class JwtTokenGenerator implements TokenGenerator {
    private readonly secret: Uint8Array;

    constructor(secret: string) {
        this.secret = new TextEncoder().encode(secret);
    }

    async generate(userId: number): Promise<string> {
        return await new jose.SignJWT({ id: userId })
            .setProtectedHeader({ alg: "HS256" })
            .setIssuedAt()
            .setExpirationTime("24h")
            .sign(this.secret);
    }

    async verify(token: string): Promise<number> {
        const { payload } = await jose.jwtVerify(token, this.secret);
        return payload.id as number;
    }
}
