from typing import Optional

from fastapi import APIRouter, Depends

from features.comment.dto import (
    CommentAuthorResponse,
    CommentResponse,
    MultipleCommentsResponseWrapper,
    NewCommentRequestWrapper,
    SingleCommentResponseWrapper,
)
from features.comment.service import create_comment, delete_comment, get_comments
from shared.errors.dto import GenericErrorResponse
from shared.web.auth import get_auth_optional, get_auth_required

router = APIRouter()


def _to_comment_response(comment) -> CommentResponse:
    return CommentResponse(
        id=comment.id,
        createdAt=comment.created_at,
        updatedAt=comment.updated_at,
        body=comment.body,
        author=CommentAuthorResponse(
            username=comment.author.username,
            bio=comment.author.bio,
            image=comment.author.image,
            following=comment.author.following,
        ),
    )


@router.post(
    "/articles/{slug}/comments",
    response_model=SingleCommentResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_create_comment(
    slug: str,
    body: NewCommentRequestWrapper,
    user_id: int = Depends(get_auth_required()),
):
    comment = await create_comment(
        slug=slug,
        author_id=user_id,
        body=body.comment.body,
    )
    return SingleCommentResponseWrapper(comment=_to_comment_response(comment))


@router.get(
    "/articles/{slug}/comments",
    response_model=MultipleCommentsResponseWrapper,
    responses={422: {"model": GenericErrorResponse}},
)
async def api_get_comments(
    slug: str,
    user_id: Optional[int] = Depends(get_auth_optional()),
):
    comments = await get_comments(slug=slug, observer_id=user_id)
    return MultipleCommentsResponseWrapper(
        comments=[_to_comment_response(c) for c in comments],
    )


@router.delete(
    "/articles/{slug}/comments/{id}",
    status_code=200,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_delete_comment(
    slug: str,
    id: int,
    user_id: int = Depends(get_auth_required()),
):
    await delete_comment(slug=slug, comment_id=id, user_id=user_id)
    return {}