import re
from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class Author:
    username: str
    bio: str
    image: Optional[str]
    following: bool = False


@dataclass
class Article:
    id: int
    slug: str
    title: str
    description: str
    body: str
    tag_list: list[str]
    created_at: datetime
    updated_at: datetime
    favorited: bool
    favorites_count: int
    author: Author

    def update(
        self,
        title: Optional[str] = None,
        description: Optional[str] = None,
        body: Optional[str] = None,
    ) -> None:
        if title is not None:
            self.slug = slugify(title)
            self.title = title
        if description is not None:
            self.description = description
        if body is not None:
            self.body = body
        self.updated_at = datetime.now(tz=None)


def slugify(title: str) -> str:
    slug = title.lower()
    slug = re.sub(r"\s+", "-", slug)
    return slug