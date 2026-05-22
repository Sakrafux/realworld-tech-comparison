from pydantic import BaseModel


class ErrorBody(BaseModel):
    body: list[str]


class GenericErrorResponse(BaseModel):
    errors: ErrorBody
