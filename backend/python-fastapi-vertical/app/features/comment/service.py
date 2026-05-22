from datetime import UTC, datetime
from typing import Optional

from features.comment.domain import Comment, CommentAuthor
from features.comment.repository import create, find_by_article_id, get_by_id, delete
from features.article.repository import find_by_slug
from features.user.repository import find_by_id as find_user_by_id
from shared.errors.app_error import (
    new_forbidden_error,
    new_resource_not_found,
)


async def create_comment(
    slug: str,
    author_id: int,
    body: str,
) -> Comment:
    article = await find_by_slug(slug)
    if not article:
        raise new_resource_not_found("Article", "slug", slug)

    user = await find_user_by_id(author_id)
    if not user:
        raise new_resource_not_found("User", "id", author_id)

    author = CommentAuthor(
        username=user.username,
        bio=user.bio,
        image=user.image,
        following=False,
    )

    comment = Comment(
        id=0,
        created_at=datetime.now(UTC),
        updated_at=datetime.now(UTC),
        body=body,
        author=author,
    )

    await create(comment, article.id, author_id)
    return comment


async def get_comments(
    slug: str,
    observer_id: Optional[int] = None,
) -> list[Comment]:
    article = await find_by_slug(slug)
    if not article:
        raise new_resource_not_found("Article", "slug", slug)

    return await find_by_article_id(article.id, observer_id)


async def delete_comment(
    slug: str,
    comment_id: int,
    user_id: int,
) -> None:
    article = await find_by_slug(slug)
    if not article:
        raise new_resource_not_found("Article", "slug", slug)

    result = await get_by_id(comment_id)
    if not result:
        raise new_resource_not_found("Comment", "id", comment_id)

    _, _, author_id = result

    if author_id != user_id:
        raise new_forbidden_error("You are not the author of this comment")

    await delete(comment_id)