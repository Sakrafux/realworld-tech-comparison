from typing import Optional

from fastapi import APIRouter, Depends, Query

from features.article.dto import (
    ArticleResponse,
    ArticleResponseWrapper,
    AuthorResponse,
    MultipleArticlesResponse,
    NewArticleRequestWrapper,
    TagsResponse,
    UpdateArticleRequestWrapper,
)
from features.article.service import (
    create_article,
    delete_article,
    favorite_article,
    get_article,
    get_articles,
    get_feed,
    unfavorite_article,
    update_article,
    get_tags,
)
from shared.errors.dto import GenericErrorResponse
from shared.web.auth import get_auth_optional, get_auth_required

router = APIRouter()


def _to_article_response(article) -> ArticleResponse:
    return ArticleResponse(
        slug=article.slug,
        title=article.title,
        description=article.description,
        body=article.body,
        tagList=article.tag_list,
        createdAt=article.created_at,
        updatedAt=article.updated_at,
        favorited=article.favorited,
        favoritesCount=article.favorites_count,
        author=AuthorResponse(
            username=article.author.username,
            bio=article.author.bio,
            image=article.author.image,
            following=article.author.following,
        ),
    )


@router.get(
    "/articles/feed",
    response_model=MultipleArticlesResponse,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_get_feed(
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    user_id: int = Depends(get_auth_required()),
):
    result = await get_feed(user_id=user_id, limit=limit, offset=offset)
    return MultipleArticlesResponse(
        articles=[_to_article_response(a) for a in result.articles],
        articlesCount=result.count,
    )


@router.get(
    "/articles",
    response_model=MultipleArticlesResponse,
    responses={422: {"model": GenericErrorResponse}},
)
async def api_get_articles(
    tag: Optional[str] = Query(None),
    author: Optional[str] = Query(None),
    favorited: Optional[str] = Query(None),
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    user_id: Optional[int] = Depends(get_auth_optional()),
):
    result = await get_articles(
        tag=tag,
        author=author,
        favorited=favorited,
        limit=limit,
        offset=offset,
        observer_id=user_id,
    )
    return MultipleArticlesResponse(
        articles=[_to_article_response(a) for a in result.articles],
        articlesCount=result.count,
    )


@router.post(
    "/articles",
    response_model=ArticleResponseWrapper,
    status_code=201,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_create_article(
    body: NewArticleRequestWrapper,
    user_id: int = Depends(get_auth_required()),
):
    article = await create_article(
        author_id=user_id,
        title=body.article.title,
        description=body.article.description,
        body=body.article.body,
        tag_list=body.article.tagList,
    )
    return ArticleResponseWrapper(article=_to_article_response(article))


@router.get(
    "/articles/{slug}",
    response_model=ArticleResponseWrapper,
    responses={422: {"model": GenericErrorResponse}},
)
async def api_get_article(
    slug: str,
    user_id: Optional[int] = Depends(get_auth_optional()),
):
    article = await get_article(slug, user_id)
    return ArticleResponseWrapper(article=_to_article_response(article))


@router.put(
    "/articles/{slug}",
    response_model=ArticleResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_update_article(
    slug: str,
    body: UpdateArticleRequestWrapper,
    user_id: int = Depends(get_auth_required()),
):
    article = await update_article(
        slug=slug,
        user_id=user_id,
        title=body.article.title,
        description=body.article.description,
        body=body.article.body,
    )
    return ArticleResponseWrapper(article=_to_article_response(article))


@router.delete(
    "/articles/{slug}",
    status_code=200,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_delete_article(
    slug: str,
    user_id: int = Depends(get_auth_required()),
):
    await delete_article(slug, user_id)
    return {}


@router.post(
    "/articles/{slug}/favorite",
    response_model=ArticleResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_favorite_article(
    slug: str,
    user_id: int = Depends(get_auth_required()),
):
    article = await favorite_article(slug, user_id)
    return ArticleResponseWrapper(article=_to_article_response(article))


@router.delete(
    "/articles/{slug}/favorite",
    response_model=ArticleResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_unfavorite_article(
    slug: str,
    user_id: int = Depends(get_auth_required()),
):
    article = await unfavorite_article(slug, user_id)
    return ArticleResponseWrapper(article=_to_article_response(article))


@router.get(
    "/tags",
    response_model=TagsResponse,
    responses={422: {"model": GenericErrorResponse}},
)
async def api_get_tags():
    tags = await get_tags()
    return TagsResponse(tags=tags)