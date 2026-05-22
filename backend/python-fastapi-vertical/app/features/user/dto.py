from typing import Optional

from pydantic import BaseModel, EmailStr, Field, HttpUrl


class LoginUserRequest(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=1)


class LoginRequestWrapper(BaseModel):
    user: LoginUserRequest


class NewUserRequest(BaseModel):
    username: str = Field(..., min_length=3, max_length=50)
    email: EmailStr = Field(..., max_length=100)
    password: str = Field(..., min_length=8, max_length=60)


class NewUserRequestWrapper(BaseModel):
    user: NewUserRequest


class UpdateUserRequest(BaseModel):
    username: Optional[str] = Field(None, min_length=3, max_length=50)
    email: Optional[EmailStr] = Field(None, max_length=100)
    password: Optional[str] = Field(None, min_length=8, max_length=60)
    bio: Optional[str] = None
    image: Optional[HttpUrl] = None


class UpdateUserRequestWrapper(BaseModel):
    user: UpdateUserRequest


class UserResponse(BaseModel):
    email: str
    token: str
    username: str
    bio: str = ""
    image: Optional[str] = None


class UserResponseWrapper(BaseModel):
    user: UserResponse


class ProfileResponse(BaseModel):
    username: str
    bio: str
    image: Optional[str] = None
    following: bool = False


class ProfileResponseWrapper(BaseModel):
    profile: ProfileResponse


class ErrorBody(BaseModel):
    body: list[str]


class GenericErrorResponse(BaseModel):
    errors: ErrorBody
