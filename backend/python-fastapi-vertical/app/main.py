from contextlib import asynccontextmanager

from fastapi import FastAPI, APIRouter
from fastapi.exceptions import RequestValidationError
from pydantic import ValidationError

from features.user.controller import router as user_router
from shared.database import close_pool, get_pool
from shared.errors import AppError
from shared.web.error_handler import app_error_handler, generic_error_handler, validation_error_handler


@asynccontextmanager
async def lifespan(app: FastAPI):
    await get_pool()
    yield
    await close_pool()


app = FastAPI(
    title="RealWorld Conduit API",
    description="Conduit API documentation",
    version="1.0.0",
    lifespan=lifespan,
)

api_router = APIRouter(prefix="/api")

api_router.include_router(user_router, tags=["users"])

app.include_router(api_router)

app.add_exception_handler(AppError, app_error_handler)
app.add_exception_handler(RequestValidationError, validation_error_handler)
app.add_exception_handler(ValidationError, validation_error_handler)
app.add_exception_handler(Exception, generic_error_handler)