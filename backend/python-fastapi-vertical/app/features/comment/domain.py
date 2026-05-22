from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass
class CommentAuthor:
    username: str
    bio: str
    image: Optional[str]
    following: bool = False


@dataclass
class Comment:
    id: int
    created_at: datetime
    updated_at: datetime
    body: str
    author: CommentAuthor