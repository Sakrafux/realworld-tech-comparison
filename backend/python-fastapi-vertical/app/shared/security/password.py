import bcrypt

from shared.errors import new_invalid_credentials_error


def hash_password(password: str) -> str:
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode("utf-8"), salt)
    return hashed.decode("utf-8")


async def compare_password(hashed_password: str, password: str) -> None:
    valid = bcrypt.checkpw(password.encode("utf-8"), hashed_password.encode("utf-8"))
    if not valid:
        raise new_invalid_credentials_error("Invalid email or password")
