from datetime import datetime
from typing import Annotated, Optional

from pydantic import BaseModel, Field


class NewArticleRequest(BaseModel):
    title: str = Field(..., min_length=1, max_length=200)
    description: str = Field(..., min_length=1, max_length=500)
    body: str = Field(..., min_length=1)
    tagList: list[Annotated[str, Field(max_length=20)]] = Field(default_factory=list)


class NewArticleRequestWrapper(BaseModel):
    article: NewArticleRequest


class UpdateArticleRequest(BaseModel):
    title: Optional[str] = Field(None, min_length=1, max_length=200)
    description: Optional[str] = Field(None, min_length=1, max_length=500)
    body: Optional[str] = Field(None, min_length=1)


class UpdateArticleRequestWrapper(BaseModel):
    article: UpdateArticleRequest


class AuthorResponse(BaseModel):
    username: str
    bio: str
    image: Optional[str] = None
    following: bool = False


class ArticleResponse(BaseModel):
    slug: str
    title: str
    description: str
    body: str
    tagList: list[str]
    createdAt: datetime
    updatedAt: datetime
    favorited: bool
    favoritesCount: int
    author: AuthorResponse


class ArticleResponseWrapper(BaseModel):
    article: ArticleResponse


class TagsResponse(BaseModel):
    tags: list[str]