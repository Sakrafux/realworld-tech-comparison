from typing import Optional

import asyncpg

from features.comment.domain import Comment, CommentAuthor
from shared.database.pool import get_pool


async def create(comment: Comment, article_id: int, author_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow(
            """
            INSERT INTO comment (body, fk_article, fk_author)
            VALUES ($1, $2, $3)
            RETURNING id, created_at, updated_at
            """,
            comment.body,
            article_id,
            author_id,
        )
        comment.id = row["id"]
        comment.created_at = row["created_at"]
        comment.updated_at = row["updated_at"]


async def find_by_article_id(article_id: int, observer_id: Optional[int] = None) -> list[Comment]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        if observer_id:
            rows = await conn.fetch(
                """
                SELECT c.id, c.body, c.created_at, c.updated_at,
                       c.fk_article, c.fk_author,
                       u.username, u.bio, u.image,
                       CASE WHEN EXISTS (
                           SELECT 1 FROM follow_is_user_to_user f
                           WHERE f.followed_user_id = u.id AND f.following_user_id = $2
                       ) THEN TRUE ELSE FALSE END as following
                FROM comment c
                JOIN app_user u ON c.fk_author = u.id
                WHERE c.fk_article = $1
                ORDER BY c.created_at DESC
                """,
                article_id,
                observer_id,
            )
        else:
            rows = await conn.fetch(
                """
                SELECT c.id, c.body, c.created_at, c.updated_at,
                       c.fk_article, c.fk_author,
                       u.username, u.bio, u.image,
                       FALSE as following
                FROM comment c
                JOIN app_user u ON c.fk_author = u.id
                WHERE c.fk_article = $1
                ORDER BY c.created_at DESC
                """,
                article_id,
            )
        return [_map_row_to_comment(row) for row in rows]


async def get_by_id(comment_id: int) -> Optional[tuple[Comment, int, int]]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow(
            """
            SELECT c.id, c.body, c.created_at, c.updated_at,
                   c.fk_article, c.fk_author,
                   u.username, u.bio, u.image,
                   FALSE as following
            FROM comment c
            JOIN app_user u ON c.fk_author = u.id
            WHERE c.id = $1
            """,
            comment_id,
        )
        if not row:
            return None
        comment = _map_row_to_comment(row)
        return comment, row["fk_article"], row["fk_author"]


async def delete(comment_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        await conn.execute("DELETE FROM comment WHERE id = $1", comment_id)


def _map_row_to_comment(row: asyncpg.Record) -> Comment:
    author = CommentAuthor(
        username=row["username"],
        bio=row.get("bio", "") or "",
        image=row.get("image"),
        following=row.get("following", False),
    )
    return Comment(
        id=row["id"],
        created_at=row["created_at"],
        updated_at=row["updated_at"],
        body=row["body"],
        author=author,
    )