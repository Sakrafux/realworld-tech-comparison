from typing import Optional

import asyncpg

from features.user.domain import Profile, User
from shared.database import get_pool


async def create(user: User) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow(
            """
            INSERT INTO app_user (username, email, password, bio, image)
            VALUES ($1, $2, $3, $4, $5)
            RETURNING id
            """,
            user.username,
            user.email,
            user.password,
            user.bio,
            user.image,
        )
        user.id = row["id"]


async def find_by_email(email: str) -> Optional[User]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow("SELECT * FROM app_user WHERE email = $1", email)
        return _map_row_to_user(row) if row else None


async def find_by_username(username: str) -> Optional[User]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow("SELECT * FROM app_user WHERE username = $1", username)
        return _map_row_to_user(row) if row else None


async def find_by_id(user_id: int) -> Optional[User]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow("SELECT * FROM app_user WHERE id = $1", user_id)
        return _map_row_to_user(row) if row else None


async def update(user: User) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        await conn.execute(
            """
            UPDATE app_user
            SET username = $1, email = $2, password = $3, bio = $4, image = $5,
                updated_at = CURRENT_TIMESTAMP, version = version + 1
            WHERE id = $6
            """,
            user.username,
            user.email,
            user.password,
            user.bio,
            user.image,
            user.id,
        )


async def get_profile_by_username(username: str, observer_id: Optional[int] = None) -> Optional[Profile]:
    pool = await get_pool()
    async with pool.acquire() as conn:
        if observer_id:
            row = await conn.fetchrow(
                """
                SELECT u.username, u.bio, u.image,
                       CASE WHEN f.following_user_id IS NOT NULL THEN TRUE ELSE FALSE END as following
                FROM app_user u
                LEFT JOIN follow_is_user_to_user f 
                    ON u.id = f.followed_user_id AND f.following_user_id = $2
                WHERE u.username = $1
                """,
                username,
                observer_id,
            )
        else:
            row = await conn.fetchrow(
                """
                SELECT username, bio, image, FALSE as following
                FROM app_user
                WHERE username = $1
                """,
                username,
            )
        return _map_row_to_profile(row) if row else None


async def follow(follower_id: int, followed_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        await conn.execute(
            """
            INSERT INTO follow_is_user_to_user (following_user_id, followed_user_id)
            VALUES ($1, $2)
            ON CONFLICT DO NOTHING
            """,
            follower_id,
            followed_id,
        )


async def unfollow(follower_id: int, followed_id: int) -> None:
    pool = await get_pool()
    async with pool.acquire() as conn:
        await conn.execute(
            """
            DELETE FROM follow_is_user_to_user
            WHERE following_user_id = $1 AND followed_user_id = $2
            """,
            follower_id,
            followed_id,
        )


def _map_row_to_user(row: asyncpg.Record) -> User:
    return User(
        id=row["id"],
        username=row["username"],
        email=row["email"],
        password=row["password"],
        bio=row.get("bio", ""),
        image=row.get("image"),
    )


def _map_row_to_profile(row: asyncpg.Record) -> Profile:
    return Profile(
        username=row["username"],
        bio=row.get("bio", ""),
        image=row.get("image"),
        following=row.get("following", False),
    )
