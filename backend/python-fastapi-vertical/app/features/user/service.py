from typing import Optional

from features.user.domain import Profile, User
from features.user.repository import find_by_email, find_by_username, create, find_by_id, update, \
    get_profile_by_username, follow, unfollow
from shared.errors import (
    new_already_exists_error,
    new_not_found_error,
    new_resource_not_found,
)
from shared.security.password import hash_password, compare_password


async def register(username: str, email: str, password: str) -> User:
    existing_email = await find_by_email(email)
    if existing_email:
        raise new_already_exists_error("Email already exists")

    existing_username = await find_by_username(username)
    if existing_username:
        raise new_already_exists_error("Username already exists")

    hashed_password = hash_password(password)
    user = User(
        id=0,
        username=username,
        email=email,
        password=hashed_password,
        bio="",
        image=None,
    )

    await create(user)
    return user


async def login(email: str, password: str) -> User:
    user = await find_by_email(email)
    if not user:
        raise new_not_found_error("User not found")

    await compare_password(user.password, password)
    return user


async def get_user(user_id: int) -> User:
    user = await find_by_id(user_id)
    if not user:
        raise new_resource_not_found("User", "id", user_id)
    return user


async def get_user_by_username(username: str) -> User:
    user = await find_by_username(username)
    if not user:
        raise new_resource_not_found("User", "username", username)
    return user


async def update_user(
    user_id: int,
    username: Optional[str] = None,
    email: Optional[str] = None,
    password: Optional[str] = None,
    bio: Optional[str] = None,
    image: Optional[str] = None,
) -> User:
    user = await get_user(user_id)

    if email and email != user.email:
        existing_email = await find_by_email(email)
        if existing_email:
            raise new_already_exists_error("Email already exists")

    if username and username != user.username:
        existing_username = await find_by_username(username)
        if existing_username:
            raise new_already_exists_error("Username already exists")

    hashed_password = None
    if password:
        hashed_password = hash_password(password)

    user.update(
        username=username,
        email=email,
        bio=bio,
        image=image,
        password=hashed_password,
    )

    await update(user)
    return user


async def get_profile(username: str, observer_id: Optional[int] = None) -> Profile:
    profile = await get_profile_by_username(username, observer_id)
    if not profile:
        raise new_resource_not_found("Profile", "username", username)
    return profile


async def follow_user(follower_id: int, username: str) -> Profile:
    user_to_follow = await find_by_username(username)
    if not user_to_follow:
        raise new_resource_not_found("User", "username", username)

    await follow(follower_id, user_to_follow.id)
    return await get_profile(username, follower_id)


async def unfollow_user(follower_id: int, username: str) -> Profile:
    user_to_unfollow = await find_by_username(username)
    if not user_to_unfollow:
        raise new_resource_not_found("User", "username", username)

    await unfollow(follower_id, user_to_unfollow.id)
    return await get_profile(username, follower_id)
