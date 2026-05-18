import type { Database } from "../../shared/database/database.js";
import { Article, type Tag } from "./article.js";
import type { ArticleRepository } from "./ports.js";

export class PostgresArticleRepository implements ArticleRepository {
    constructor(private db: Database) {}

    async create(article: Article, authorId: number): Promise<void> {
        await this.db.tx(async (t) => {
            const row = await t.one(
                `INSERT INTO article (slug, title, description, body, fk_author)
                 VALUES ($1, $2, $3, $4, $5)
                 RETURNING id`,
                [article.slug, article.title, article.description, article.body, authorId],
            );
            article.id = parseInt(row.id);

            for (const tagName of article.tagList) {
                let tagRow = await t.oneOrNone("SELECT id FROM tag WHERE tag = $1", [tagName]);
                let tagId: number;
                if (!tagRow) {
                    tagRow = await t.one("INSERT INTO tag (tag) VALUES ($1) RETURNING id", [
                        tagName,
                    ]);
                }
                tagId = parseInt(tagRow.id);

                await t.none(
                    "INSERT INTO tag_is_article_to_tag (article_id, tag_id) VALUES ($1, $2) ON CONFLICT DO NOTHING",
                    [article.id, tagId],
                );
            }
        });
    }

    async getBySlug(slug: string, observerId?: number): Promise<Article | null> {
        const query = `
            SELECT a.*, u.username, u.bio, u.image,
                   (SELECT COUNT(*) FROM favorite_is_article_to_user f WHERE f.article_id = a.id) as favorites_count,
                   ${observerId ? `CASE WHEN EXISTS (SELECT 1 FROM favorite_is_article_to_user f WHERE f.article_id = a.id AND f.user_id = $2) THEN TRUE ELSE FALSE END` : "FALSE"} as favorited,
                   ${observerId ? `CASE WHEN EXISTS (SELECT 1 FROM follow_is_user_to_user f WHERE f.followed_user_id = u.id AND f.following_user_id = $2) THEN TRUE ELSE FALSE END` : "FALSE"} as following,
                   ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
            WHERE a.slug = $1
        `;
        const row = await this.db.oneOrNone(query, [slug, observerId]);
        return row ? this.mapRowToArticle(row) : null;
    }

    async getByTitle(title: string, observerId?: number): Promise<Article | null> {
        const query = `
            SELECT a.*, u.username, u.bio, u.image,
                   (SELECT COUNT(*) FROM favorite_is_article_to_user f WHERE f.article_id = a.id) as favorites_count,
                   ${observerId ? `CASE WHEN EXISTS (SELECT 1 FROM favorite_is_article_to_user f WHERE f.article_id = a.id AND f.user_id = $2) THEN TRUE ELSE FALSE END` : "FALSE"} as favorited,
                   ${observerId ? `CASE WHEN EXISTS (SELECT 1 FROM follow_is_user_to_user f WHERE f.followed_user_id = u.id AND f.following_user_id = $2) THEN TRUE ELSE FALSE END` : "FALSE"} as following,
                   ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
            WHERE a.title = $1
        `;
        const row = await this.db.oneOrNone(query, [title, observerId]);
        return row ? this.mapRowToArticle(row) : null;
    }

    async findAll(params: {
        tag?: string;
        author?: string;
        favorited?: string;
        limit: number;
        offset: number;
        observerId?: number;
    }): Promise<{ articles: Article[]; articlesCount: number }> {
        let queryBase = `
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
        `;
        const args: any[] = [];
        let argIndex = 1;

        if (params.observerId) {
            queryBase += `
                LEFT JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $${argIndex++}
                LEFT JOIN favorite_is_article_to_user fav_obs ON a.id = fav_obs.article_id AND fav_obs.user_id = $${argIndex - 1}
            `;
            args.push(params.observerId);
        }

        if (params.tag) {
            queryBase += `
                JOIN tag_is_article_to_tag tat ON a.id = tat.article_id
                JOIN tag t ON tat.tag_id = t.id AND t.tag = $${argIndex++}
            `;
            args.push(params.tag);
        }

        if (params.favorited) {
            queryBase += `
                JOIN favorite_is_article_to_user fav ON a.id = fav.article_id
                JOIN app_user u_fav ON fav.user_id = u_fav.id AND u_fav.username = $${argIndex++}
            `;
            args.push(params.favorited);
        }

        let whereClause = " WHERE 1=1";
        if (params.author) {
            whereClause += ` AND u.username = $${argIndex++}`;
            args.push(params.author);
        }

        const countQuery = `SELECT COUNT(DISTINCT a.id) ` + queryBase + whereClause;
        const countRow = await this.db.one(countQuery, args);
        const count = parseInt(countRow.count);

        if (count === 0) {
            return { articles: [], articlesCount: 0 };
        }

        let selectQuery = `
            SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
                   u.username, u.bio, u.image,
                   (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count,
                   ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list,
        `;

        if (params.observerId) {
            selectQuery += `
                   CASE WHEN f.following_user_id IS NOT NULL THEN TRUE ELSE FALSE END as following,
                   CASE WHEN fav_obs.user_id IS NOT NULL THEN TRUE ELSE FALSE END as favorited
            `;
        } else {
            selectQuery += `
                   FALSE as following,
                   FALSE as favorited
            `;
        }

        selectQuery += queryBase + whereClause + ` ORDER BY a.created_at DESC LIMIT $${argIndex++} OFFSET $${argIndex++}`;
        args.push(params.limit, params.offset);

        const rows = await this.db.manyOrNone(selectQuery, args);
        return {
            articles: rows.map((row) => this.mapRowToArticle(row)),
            articlesCount: count,
        };
    }

    async findFeed(params: {
        limit: number;
        offset: number;
        userId: number;
    }): Promise<{ articles: Article[]; articlesCount: number }> {
        const query = `
            SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
                   u.username, u.bio, u.image,
                   TRUE as following,
                   CASE WHEN EXISTS (SELECT 1 FROM favorite_is_article_to_user fav WHERE fav.article_id = a.id AND fav.user_id = $1) THEN TRUE ELSE FALSE END as favorited,
                   (SELECT COUNT(*) FROM favorite_is_article_to_user f WHERE f.article_id = a.id) as favorites_count,
                   ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
            JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $1
            ORDER BY a.created_at DESC
            LIMIT $2 OFFSET $3
        `;

        const countQuery = `
            SELECT COUNT(*)
            FROM article a
            JOIN follow_is_user_to_user f ON a.fk_author = f.followed_user_id AND f.following_user_id = $1
        `;

        const [rows, countRow] = await Promise.all([
            this.db.manyOrNone(query, [params.userId, params.limit, params.offset]),
            this.db.one(countQuery, [params.userId]),
        ]);

        return {
            articles: rows.map((row) => this.mapRowToArticle(row)),
            articlesCount: parseInt(countRow.count),
        };
    }

    async update(article: Article): Promise<void> {
        await this.db.none(
            `UPDATE article
             SET slug = $1, title = $2, description = $3, body = $4,
                 updated_at = CURRENT_TIMESTAMP, version = version + 1
             WHERE id = $5`,
            [article.slug, article.title, article.description, article.body, article.id],
        );
    }

    async delete(id: number): Promise<void> {
        await this.db.tx(async (t) => {
            await t.none("DELETE FROM tag_is_article_to_tag WHERE article_id = $1", [id]);
            await t.none("DELETE FROM favorite_is_article_to_user WHERE article_id = $1", [id]);
            await t.none("DELETE FROM comment WHERE fk_article = $1", [id]);
            await t.none("DELETE FROM article WHERE id = $1", [id]);
        });
    }

    async favorite(articleId: number, userId: number): Promise<void> {
        await this.db.none(
            "INSERT INTO favorite_is_article_to_user (article_id, user_id) VALUES ($1, $2) ON CONFLICT DO NOTHING",
            [articleId, userId],
        );
    }

    async unfavorite(articleId: number, userId: number): Promise<void> {
        await this.db.none(
            "DELETE FROM favorite_is_article_to_user WHERE article_id = $1 AND user_id = $2",
            [articleId, userId],
        );
    }

    async findAllTags(): Promise<Tag[]> {
        const rows = await this.db.manyOrNone("SELECT tag as name FROM tag");
        return rows as Tag[];
    }

    private mapRowToArticle(row: any): Article {
        return new Article(
            parseInt(row.id),
            row.slug,
            row.title,
            row.description,
            row.body,
            row.tag_list || [],
            row.created_at,
            row.updated_at,
            row.favorited,
            parseInt(row.favorites_count),
            {
                username: row.username,
                bio: row.bio,
                image: row.image,
                following: row.following,
            },
        );
    }
}
