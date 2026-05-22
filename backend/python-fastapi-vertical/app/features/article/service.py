from datetime import UTC
from typing import Optional

from features.article.domain import Article, Author, slugify
from features.article.repository import (
    ArticlesList,
    create,
    find_by_slug,
    find_by_slug_for_author,
    find_all,
    find_feed,
    update,
    delete,
    favorite,
    unfavorite,
    find_all_tags,
)
from features.user.repository import find_by_id as find_user_by_id, find_by_username
from shared.errors.app_error import (
    new_already_exists_error,
    new_forbidden_error,
    new_resource_not_found,
)


async def create_article(
    author_id: int,
    title: str,
    description: str,
    body: str,
    tag_list: Optional[list[str]] = None,
) -> Article:
    slug = slugify(title)
    existing = await find_by_slug(slug)
    if existing:
        raise new_already_exists_error("Article with this title already exists")

    user = await find_user_by_id(author_id)
    if not user:
        raise new_resource_not_found("User", "id", author_id)

    author = Author(
        username=user.username,
        bio=user.bio,
        image=user.image,
        following=False,
    )

    from datetime import datetime

    article = Article(
        id=0,
        slug=slug,
        title=title,
        description=description,
        body=body,
        tag_list=tag_list or [],
        created_at=datetime.now(UTC),
        updated_at=datetime.now(UTC),
        favorited=False,
        favorites_count=0,
        author=author,
    )

    await create(article, author_id)
    return article


async def get_article(slug: str, observer_id: Optional[int] = None) -> Article:
    article = await find_by_slug(slug, observer_id)
    if not article:
        raise new_resource_not_found("Article", "slug", slug)
    return article


async def update_article(
    slug: str,
    user_id: int,
    title: Optional[str] = None,
    description: Optional[str] = None,
    body: Optional[str] = None,
) -> Article:
    result = await find_by_slug_for_author(slug)
    if not result:
        raise new_resource_not_found("Article", "slug", slug)

    article, author_id = result

    if author_id != user_id:
        raise new_forbidden_error("You are not allowed to update this article")

    if title is not None:
        new_slug = slugify(title)
        if new_slug != article.slug:
            existing = await find_by_slug(new_slug)
            if existing:
                raise new_already_exists_error("Article with this title already exists")

    article.update(title=title, description=description, body=body)
    await update(article)
    return article


async def delete_article(slug: str, user_id: int) -> None:
    result = await find_by_slug_for_author(slug)
    if not result:
        raise new_resource_not_found("Article", "slug", slug)

    article, author_id = result

    if author_id != user_id:
        raise new_forbidden_error("You are not allowed to delete this article")

    await delete(article.id)


async def favorite_article(slug: str, user_id: int) -> Article:
    article = await find_by_slug(slug, user_id)
    if not article:
        raise new_resource_not_found("Article", "slug", slug)

    await favorite(article.id, user_id)

    refreshed = await find_by_slug(slug, user_id)
    return refreshed


async def unfavorite_article(slug: str, user_id: int) -> Article:
    article = await find_by_slug(slug, user_id)
    if not article:
        raise new_resource_not_found("Article", "slug", slug)

    await unfavorite(article.id, user_id)

    refreshed = await find_by_slug(slug, user_id)
    return refreshed


async def get_tags() -> list[str]:
    return await find_all_tags()


async def get_articles(
    tag: Optional[str] = None,
    author: Optional[str] = None,
    favorited: Optional[str] = None,
    limit: int = 20,
    offset: int = 0,
    observer_id: Optional[int] = None,
) -> ArticlesList:
    return await find_all(
        tag=tag,
        author=author,
        favorited=favorited,
        limit=limit,
        offset=offset,
        observer_id=observer_id,
    )


async def get_feed(
    user_id: int,
    limit: int = 20,
    offset: int = 0,
) -> ArticlesList:
    return await find_feed(
        user_id=user_id,
        limit=limit,
        offset=offset,
    )