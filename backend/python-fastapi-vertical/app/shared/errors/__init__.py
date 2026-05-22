from enum import Enum
from typing import Any


class ErrorType(str, Enum):
    NOT_FOUND = "NOT_FOUND"
    ALREADY_EXISTS = "ALREADY_EXISTS"
    INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
    UNAUTHORIZED = "UNAUTHORIZED"
    FORBIDDEN = "FORBIDDEN"
    INTERNAL = "INTERNAL"
    UNPROCESSABLE = "UNPROCESSABLE"


class AppError(Exception):
    def __init__(self, error_type: ErrorType, message: str):
        self.error_type = error_type
        self.message = message
        super().__init__(message)


def new_not_found_error(message: str) -> AppError:
    return AppError(ErrorType.NOT_FOUND, message)


def new_already_exists_error(message: str) -> AppError:
    return AppError(ErrorType.ALREADY_EXISTS, message)


def new_invalid_credentials_error(message: str) -> AppError:
    return AppError(ErrorType.INVALID_CREDENTIALS, message)


def new_unauthorized_error(message: str) -> AppError:
    return AppError(ErrorType.UNAUTHORIZED, message)


def new_forbidden_error(message: str) -> AppError:
    return AppError(ErrorType.FORBIDDEN, message)


def new_unprocessable_entity_error(message: str) -> AppError:
    return AppError(ErrorType.UNPROCESSABLE, message)


def new_internal_error(message: str) -> AppError:
    return AppError(ErrorType.INTERNAL, message)


def new_resource_not_found(resource: str, field: str, value: Any) -> AppError:
    return new_not_found_error(f"{resource} not found with {field}: '{value}'")
