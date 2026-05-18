import type { Database } from "../../shared/database/database.js";
import { Comment } from "./comment.domain.js";
import type { CommentRepository } from "./comment.ports.js";

export class PostgresCommentRepository implements CommentRepository {
    constructor(private db: Database) {}

    async create(comment: Comment, articleId: number, authorId: number): Promise<void> {
        const row = await this.db.one(
            `INSERT INTO comment (body, fk_article, fk_author)
             VALUES ($1, $2, $3)
             RETURNING id`,
            [comment.body, articleId, authorId],
        );
        comment.id = parseInt(row.id);
    }

    async findByArticleId(articleId: number, observerId?: number): Promise<Comment[]> {
        const query = `
            SELECT c.*, u.username, u.bio, u.image,
                   ${observerId ? `CASE WHEN EXISTS (SELECT 1 FROM follow_is_user_to_user f WHERE f.followed_user_id = u.id AND f.following_user_id = $2) THEN TRUE ELSE FALSE END` : "FALSE"} as following
            FROM comment c
            JOIN app_user u ON c.fk_author = u.id
            WHERE c.fk_article = $1
            ORDER BY c.created_at DESC
        `;
        const rows = await this.db.manyOrNone(query, [articleId, observerId]);
        return rows.map((row) => this.mapRowToComment(row));
    }

    async getById(id: number): Promise<{ comment: Comment; articleId: number; authorId: number } | null> {
        const query = `
            SELECT c.*, u.username, u.bio, u.image, FALSE as following
            FROM comment c
            JOIN app_user u ON c.fk_author = u.id
            WHERE c.id = $1
        `;
        const row = await this.db.oneOrNone(query, [id]);
        if (!row) return null;

        return {
            comment: this.mapRowToComment(row),
            articleId: parseInt(row.fk_article),
            authorId: parseInt(row.fk_author),
        };
    }

    async delete(id: number): Promise<void> {
        await this.db.none("DELETE FROM comment WHERE id = $1", [id]);
    }

    private mapRowToComment(row: any): Comment {
        return new Comment(
            parseInt(row.id),
            row.created_at,
            row.updated_at,
            row.body,
            {
                username: row.username,
                bio: row.bio,
                image: row.image,
                following: row.following,
            },
        );
    }
}
