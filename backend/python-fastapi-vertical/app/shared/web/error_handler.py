import logging

from fastapi import Request
from fastapi.responses import JSONResponse
from pydantic import ValidationError

from shared.errors import AppError, ErrorType


status_code_map = {
    ErrorType.NOT_FOUND: 404,
    ErrorType.ALREADY_EXISTS: 422,
    ErrorType.INVALID_CREDENTIALS: 401,
    ErrorType.UNAUTHORIZED: 401,
    ErrorType.FORBIDDEN: 403,
    ErrorType.UNPROCESSABLE: 422,
    ErrorType.INTERNAL: 500,
}


async def app_error_handler(request: Request, exc: AppError) -> JSONResponse:
    status_code = status_code_map.get(exc.error_type, 500)
    return JSONResponse(
        status_code=status_code,
        content={"errors": {"body": [exc.message]}},
    )


async def validation_error_handler(request: Request, exc: ValidationError) -> JSONResponse:
    messages = [f"{'.'.join(str(loc) for loc in error['loc'])} {error['msg']}" for error in exc.errors()]
    logging.error(messages)
    return JSONResponse(
        status_code=422,
        content={"errors": {"body": messages}},
    )


async def generic_error_handler(request: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(
        status_code=500,
        content={"errors": {"body": [str(exc)]}},
    )
