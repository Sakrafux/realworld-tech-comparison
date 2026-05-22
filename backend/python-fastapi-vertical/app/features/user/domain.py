from dataclasses import dataclass
from typing import Optional


@dataclass
class User:
    id: int
    username: str
    email: str
    password: str
    bio: str = ""
    image: Optional[str] = None

    def update(
        self,
        username: Optional[str] = None,
        email: Optional[str] = None,
        bio: Optional[str] = None,
        image: Optional[str] = None,
        password: Optional[str] = None,
    ) -> None:
        if username is not None:
            self.username = username
        if email is not None:
            self.email = email
        if bio is not None:
            self.bio = bio
        if image is not None:
            self.image = image
        if password is not None:
            self.password = password


@dataclass
class Profile:
    username: str
    bio: str
    image: Optional[str]
    following: bool = False
