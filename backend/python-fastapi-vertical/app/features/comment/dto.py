from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class NewCommentRequest(BaseModel):
    body: str = Field(..., min_length=1)


class NewCommentRequestWrapper(BaseModel):
    comment: NewCommentRequest


class CommentAuthorResponse(BaseModel):
    username: str
    bio: str
    image: Optional[str] = None
    following: bool = False


class CommentResponse(BaseModel):
    id: int
    createdAt: datetime
    updatedAt: datetime
    body: str
    author: CommentAuthorResponse


class SingleCommentResponseWrapper(BaseModel):
    comment: CommentResponse


class MultipleCommentsResponseWrapper(BaseModel):
    comments: list[CommentResponse]