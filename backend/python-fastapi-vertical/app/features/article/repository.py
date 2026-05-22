from typing import Optional

import asyncpg

from features.article.domain import Article, Author
from shared.database.pool import get_pool


class ArticlesList:
    def __init__(self, articles: list[Article], count: int):
        self.articles = articles
        self.count = count


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


async def find_all(
    tag: Optional[str] = None,
    author: Optional[str] = None,
    favorited: Optional[str] = None,
    limit: int = 20,
    offset: int = 0,
    observer_id: Optional[int] = None,
) -> ArticlesList:
    pool = await get_pool()
    async with pool.acquire() as conn:
        args = []
        arg_idx = 1

        join_clauses = ""
        where_clauses = ""

        if observer_id:
            join_clauses += f"""
            LEFT JOIN follow_is_user_to_user f ON a.fk_author = f.followed_user_id AND f.following_user_id = ${arg_idx}
            LEFT JOIN favorite_is_article_to_user fav_obs ON a.id = fav_obs.article_id AND fav_obs.user_id = ${arg_idx}
            """
            args.append(observer_id)
            arg_idx += 1

        if tag:
            join_clauses += f"""
            JOIN tag_is_article_to_tag tat_{arg_idx} ON a.id = tat_{arg_idx}.article_id
            JOIN tag t_{arg_idx} ON t_{arg_idx}.id = tat_{arg_idx}.tag_id AND t_{arg_idx}.tag = ${arg_idx}
            """
            args.append(tag)
            arg_idx += 1

        if favorited:
            join_clauses += f"""
            JOIN favorite_is_article_to_user fav_{arg_idx} ON a.id = fav_{arg_idx}.article_id
            JOIN app_user u_fav_{arg_idx} ON u_fav_{arg_idx}.id = fav_{arg_idx}.user_id AND u_fav_{arg_idx}.username = ${arg_idx}
            """
            args.append(favorited)
            arg_idx += 1

        if author:
            where_clauses += f" AND u.username = ${arg_idx}"
            args.append(author)
            arg_idx += 1

        if observer_id:
            following_expr = "CASE WHEN f.following_user_id IS NOT NULL THEN TRUE ELSE FALSE END"
            favorited_expr = "CASE WHEN fav_obs.user_id IS NOT NULL THEN TRUE ELSE FALSE END"
        else:
            following_expr = "FALSE"
            favorited_expr = "FALSE"

        count_args = list(args)
        count_query = f"""
            SELECT COUNT(DISTINCT a.id)
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
            {join_clauses}
            WHERE 1=1{where_clauses}
            """
        count_row = await conn.fetchrow(count_query, *count_args)
        total_count = count_row["count"]

        if total_count == 0:
            return ArticlesList(articles=[], count=0)

        data_args = list(args)
        data_args.append(limit)
        data_args.append(offset)
        data_query = f"""
            SELECT a.id, a.slug, a.title, a.description, a.body,
                   a.created_at, a.updated_at,
                   (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count,
                   {favorited_expr} as favorited,
                   u.username, u.bio, u.image,
                   {following_expr} as following,
                   ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
            {join_clauses}
            WHERE 1=1{where_clauses}
            ORDER BY a.created_at DESC
            LIMIT ${arg_idx} OFFSET ${arg_idx + 1}
            """
        rows = await conn.fetch(data_query, *data_args)
        articles = [_map_row_to_article(row) for row in rows]
        return ArticlesList(articles=articles, count=total_count)


async def find_feed(
    user_id: int,
    limit: int = 20,
    offset: int = 0,
) -> ArticlesList:
    pool = await get_pool()
    async with pool.acquire() as conn:
        count_row = await conn.fetchrow(
            """
            SELECT COUNT(*)
            FROM article a
            JOIN follow_is_user_to_user f ON a.fk_author = f.followed_user_id AND f.following_user_id = $1
            """,
            user_id,
        )
        total_count = count_row["count"]

        if total_count == 0:
            return ArticlesList(articles=[], count=0)

        rows = await conn.fetch(
            """
            SELECT a.id, a.slug, a.title, a.description, a.body,
                   a.created_at, a.updated_at,
                   (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count,
                   (SELECT EXISTS(SELECT 1 FROM favorite_is_article_to_user WHERE article_id = a.id AND user_id = $1)) as favorited,
                   u.username, u.bio, u.image,
                   TRUE as following,
                   ARRAY(SELECT t.tag FROM tag t JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id WHERE tat.article_id = a.id) as tag_list
            FROM article a
            JOIN app_user u ON a.fk_author = u.id
            JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $1
            ORDER BY a.created_at DESC
            LIMIT $2 OFFSET $3
            """,
            user_id,
            limit,
            offset,
        )
        articles = [_map_row_to_article(row) for row in rows]
        return ArticlesList(articles=articles, count=total_count)


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