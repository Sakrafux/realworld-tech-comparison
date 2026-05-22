import datetime

import jwt

from shared.config.env import settings
from shared.errors.app_error import new_unauthorized_error


def generate_token(user_id: int) -> str:
    payload = {
        "id": user_id,
        "iat": datetime.datetime.now(datetime.timezone.utc),
        "exp": datetime.datetime.now(datetime.timezone.utc)
        + datetime.timedelta(hours=settings.JWT_EXPIRATION_HOURS),
    }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)


def verify_token(token: str) -> int:
    try:
        payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])
        return payload["id"]
    except jwt.ExpiredSignatureError:
        raise new_unauthorized_error("Token has expired")
    except jwt.InvalidTokenError:
        raise new_unauthorized_error("Invalid or expired token")
