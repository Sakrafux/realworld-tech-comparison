from typing import Optional

import asyncpg

from features.article.domain import Article, Author
from shared.database.pool import get_pool


async def create(article: Article, author_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        async with conn.transaction():
            row = await conn.fetchrow(
                """
                INSERT INTO article (slug, title, description, body, fk_author)
                VALUES ($1, $2, $3, $4, $5)
                RETURNING id, created_at, updated_at
                """,
                article.slug,
                article.title,
                article.description,
                article.body,
                author_id,
            )
            article.id = row["id"]
            article.created_at = row["created_at"]
            article.updated_at = row["updated_at"]

            for tag_name in article.tag_list:
                tag_row = await conn.fetchrow(
                    "SELECT id FROM tag WHERE tag = $1",
                    tag_name,
                )
                if not tag_row:
                    tag_row = await conn.fetchrow(
                        "INSERT INTO tag (tag) VALUES ($1) RETURNING id",
                        tag_name,
                    )
                await conn.execute(
                    """
                    INSERT INTO tag_is_article_to_tag (article_id, tag_id)
                    VALUES ($1, $2)
                    ON CONFLICT DO NOTHING
                    """,
                    article.id,
                    tag_row["id"],
                )


async def find_by_slug(slug: str, observer_id: Optional[int] = None) -> Optional[Article]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        if observer_id:
            row = await conn.fetchrow(
                """
                SELECT a.id, a.slug, a.title, a.description, a.body,
                       a.created_at, a.updated_at,
                       (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count,
                       (SELECT EXISTS(SELECT 1 FROM favorite_is_article_to_user WHERE article_id = a.id AND user_id = $2)) as favorited,
                       u.username, u.bio, u.image,
                       (SELECT EXISTS(SELECT 1 FROM follow_is_user_to_user WHERE followed_user_id = a.fk_author AND following_user_id = $2)) as following,
                       ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
                FROM article a
                JOIN app_user u ON a.fk_author = u.id
                WHERE a.slug = $1
                """,
                slug,
                observer_id,
            )
        else:
            row = await conn.fetchrow(
                """
                SELECT a.id, a.slug, a.title, a.description, a.body,
                       a.created_at, a.updated_at,
                       (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count,
                       FALSE as favorited,
                       u.username, u.bio, u.image,
                       FALSE as following,
                       ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
                FROM article a
                JOIN app_user u ON a.fk_author = u.id
                WHERE a.slug = $1
                """,
                slug,
            )
        return _map_row_to_article(row) if row else None


async def find_by_slug_for_author(slug: str) -> Optional[tuple[Article, int]]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow(
            """
            SELECT a.id, a.slug, a.title, a.description, a.body,
                   a.fk_author,
                   a.created_at, a.updated_at,
                   (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count,
                   0 as favorited,
                   u.username, u.bio, u.image,
                   FALSE as following,
                   ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
            WHERE a.slug = $1
            """,
            slug,
        )
        if not row:
            return None
        author_id = row["fk_author"]
        return _map_row_to_article(row), author_id


async def update(article: Article) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        await conn.execute(
            """
            UPDATE article
            SET slug = $1, title = $2, description = $3, body = $4,
                updated_at = CURRENT_TIMESTAMP, version = version + 1
            WHERE id = $5
            """,
            article.slug,
            article.title,
            article.description,
            article.body,
            article.id,
        )


async def delete(article_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        async with conn.transaction():
            await conn.execute(
                "DELETE FROM tag_is_article_to_tag WHERE article_id = $1",
                article_id,
            )
            await conn.execute(
                "DELETE FROM favorite_is_article_to_user WHERE article_id = $1",
                article_id,
            )
            await conn.execute(
                "DELETE FROM article WHERE id = $1",
                article_id,
            )


async def favorite(article_id: int, user_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        await conn.execute(
            """
            INSERT INTO favorite_is_article_to_user (article_id, user_id)
            VALUES ($1, $2)
            ON CONFLICT DO NOTHING
            """,
            article_id,
            user_id,
        )


async def unfavorite(article_id: int, user_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        await conn.execute(
            """
            DELETE FROM favorite_is_article_to_user
            WHERE article_id = $1 AND user_id = $2
            """,
            article_id,
            user_id,
        )


async def find_all_tags() -> list[str]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        rows = await conn.fetch("SELECT tag FROM tag")
        return [row["tag"] for row in rows]


def _map_row_to_article(row: asyncpg.Record) -> Article:
    author = Author(
        username=row["username"],
        bio=row.get("bio", "") or "",
        image=row.get("image"),
        following=row.get("following", False),
    )
    return Article(
        id=row["id"],
        slug=row["slug"],
        title=row["title"],
        description=row["description"],
        body=row["body"],
        tag_list=row.get("tag_list", []) or [],
        created_at=row["created_at"],
        updated_at=row["updated_at"],
        favorited=row.get("favorited", False),
        favorites_count=row.get("favorites_count", 0),
        author=author,
    )