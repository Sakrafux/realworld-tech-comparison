import type { Database } from "../../shared/database/database.js";
import { User, type Profile } from "./user.js";
import type { UserRepository } from "./ports.js";

export class PostgresUserRepository implements UserRepository {
    constructor(private db: Database) {}

    async create(user: User): Promise<void> {
        const row = await this.db.one(
            `INSERT INTO app_user (username, email, password, bio, image)
             VALUES ($1, $2, $3, $4, $5)
             RETURNING id`,
            [user.username, user.email, user.password, user.bio, user.image],
        );
        user.id = parseInt(row.id);
    }

    async findByEmail(email: string): Promise<User | null> {
        const row = await this.db.oneOrNone("SELECT * FROM app_user WHERE email = $1", [email]);
        return row ? this.mapRowToUser(row) : null;
    }

    async findByUsername(username: string): Promise<User | null> {
        const row = await this.db.oneOrNone("SELECT * FROM app_user WHERE username = $1", [username]);
        return row ? this.mapRowToUser(row) : null;
    }

    async findById(id: number): Promise<User | null> {
        const row = await this.db.oneOrNone("SELECT * FROM app_user WHERE id = $1", [id]);
        return row ? this.mapRowToUser(row) : null;
    }

    async update(user: User): Promise<void> {
        await this.db.none(
            `UPDATE app_user
             SET username = $1, email = $2, password = $3, bio = $4, image = $5,
                 updated_at = CURRENT_TIMESTAMP, version = version + 1
             WHERE id = $6`,
            [user.username, user.email, user.password, user.bio, user.image, user.id],
        );
    }

    async getProfileByUsername(username: string, observerId?: number): Promise<Profile | null> {
        let query: string;
        let args: any[];

        if (observerId) {
            query = `
                SELECT u.username, u.bio, u.image,
                CASE WHEN f.following_user_id IS NOT NULL THEN TRUE ELSE FALSE END as following
                FROM app_user u
                LEFT JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $2
                WHERE u.username = $1
            `;
            args = [username, observerId];
        } else {
            query = `
                SELECT username, bio, image, FALSE as following
                FROM app_user
                WHERE username = $1
            `;
            args = [username];
        }

        const row = await this.db.oneOrNone(query, args);
        return row ? (row as Profile) : null;
    }

    async follow(followerId: number, followedId: number): Promise<void> {
        await this.db.none(
            "INSERT INTO follow_is_user_to_user (following_user_id, followed_user_id) VALUES ($1, $2) ON CONFLICT DO NOTHING",
            [followerId, followedId],
        );
    }

    async unfollow(followerId: number, followedId: number): Promise<void> {
        await this.db.none(
            "DELETE FROM follow_is_user_to_user WHERE following_user_id = $1 AND followed_user_id = $2",
            [followerId, followedId],
        );
    }

    private mapRowToUser(row: any): User {
        return new User(
            parseInt(row.id),
            row.username,
            row.email,
            row.password,
            row.bio,
            row.image,
        );
    }
}
