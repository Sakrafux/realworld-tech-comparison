from typing import Optional

from fastapi import Header

from shared.errors import new_unauthorized_error
from shared.security.token import verify_token


class AuthDependency:
    def __init__(self, required: bool = True):
        self.required = required

    async def __call__(
        self,
        authorization: Optional[str] = Header(None),
    ) -> Optional[int]:
        if not authorization:
            if self.required:
                raise new_unauthorized_error("Authorization header is required")
            return None

        parts = authorization.split(" ")
        if len(parts) != 2 or parts[0] != "Token":
            if self.required:
                raise new_unauthorized_error("Invalid authorization header format")
            return None

        token = parts[1]
        if not token:
            if self.required:
                raise new_unauthorized_error("Token is required")
            return None

        try:
            user_id = verify_token(token)
            return user_id
        except Exception:
            if self.required:
                raise new_unauthorized_error("Invalid or expired token")
            return None


def get_auth_required() -> AuthDependency:
    return AuthDependency(required=True)


def get_auth_optional() -> AuthDependency:
    return AuthDependency(required=False)
