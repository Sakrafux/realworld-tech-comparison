from typing import Optional

from fastapi import APIRouter, Depends, Request

from features.user.dto import (
    GenericErrorResponse,
    LoginRequestWrapper,
    NewUserRequestWrapper,
    ProfileResponse,
    ProfileResponseWrapper,
    UpdateUserRequestWrapper,
    UserResponse,
    UserResponseWrapper,
)
from features.user.service import unfollow_user, follow_user, get_profile, update_user, get_user, register, login
from shared.security.token import generate_token
from shared.web.auth import get_auth_optional, get_auth_required

router = APIRouter()


@router.post(
    "/users/login",
    response_model=UserResponseWrapper,
    status_code=200,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_login(
        body: LoginRequestWrapper
):
    user = await login(email=body.user.email, password=body.user.password)
    token = generate_token(user.id)
    return UserResponseWrapper(
        user=UserResponse(
            email=user.email,
            token=token,
            username=user.username,
            bio=user.bio,
            image=user.image,
        )
    )


@router.post(
    "/users",
    response_model=UserResponseWrapper,
    status_code=201,
    responses={422: {"model": GenericErrorResponse}},
)
async def api_register(
        body: NewUserRequestWrapper
):
    user = await register(
        username=body.user.username,
        email=body.user.email,
        password=body.user.password,
    )
    token = generate_token(user.id)
    return UserResponseWrapper(
        user=UserResponse(
            email=user.email,
            token=token,
            username=user.username,
            bio=user.bio,
            image=user.image,
        )
    )


@router.get(
    "/user",
    response_model=UserResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_get_current_user(
    request: Request,
    user_id: int = Depends(get_auth_required()),
):
    user = await get_user(user_id)
    auth_header = request.headers.get("authorization", "")
    token = auth_header.split(" ")[1]
    return UserResponseWrapper(
        user=UserResponse(
            email=user.email,
            token=token,
            username=user.username,
            bio=user.bio,
            image=user.image,
        )
    )


@router.put(
    "/user",
    response_model=UserResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_update_current_user(
    request: Request,
    body: UpdateUserRequestWrapper,
    user_id: int = Depends(get_auth_required()),
):
    user = await update_user(
        user_id=user_id,
        username=body.user.username,
        email=body.user.email,
        password=body.user.password,
        bio=body.user.bio,
        image=str(body.user.image) if body.user.image else None,
    )
    auth_header = request.headers.get("authorization", "")
    token = auth_header.split(" ")[1]
    return UserResponseWrapper(
        user=UserResponse(
            email=user.email,
            token=token,
            username=user.username,
            bio=user.bio,
            image=user.image,
        )
    )


@router.get(
    "/profiles/{username}",
    response_model=ProfileResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_get_profile(
        username: str,
        user_id: Optional[int] = Depends(get_auth_optional()),
):
    profile = await get_profile(username, user_id)
    return ProfileResponseWrapper(
        profile=ProfileResponse(
            username=profile.username,
            bio=profile.bio,
            image=profile.image,
            following=profile.following,
        )
    )


@router.post(
    "/profiles/{username}/follow",
    response_model=ProfileResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_follow_user(
        username: str,
        user_id: int = Depends(get_auth_required()),
):
    profile = await follow_user(user_id, username)
    return ProfileResponseWrapper(
        profile=ProfileResponse(
            username=profile.username,
            bio=profile.bio,
            image=profile.image,
            following=profile.following,
        )
    )


@router.delete(
    "/profiles/{username}/follow",
    response_model=ProfileResponseWrapper,
    responses={401: {"model": GenericErrorResponse}, 422: {"model": GenericErrorResponse}},
)
async def api_unfollow_user(
        username: str,
        user_id: int = Depends(get_auth_required()),
):
    profile = await unfollow_user(user_id, username)
    return ProfileResponseWrapper(
        profile=ProfileResponse(
            username=profile.username,
            bio=profile.bio,
            image=profile.image,
            following=profile.following,
        )
    )
