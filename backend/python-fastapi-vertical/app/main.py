from contextlib import asynccontextmanager

from fastapi import FastAPI, APIRouter
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from pydantic import ValidationError

from features.article.controller import router as article_router
from features.comment.controller import router as comment_router
from features.user.controller import router as user_router
from shared.config.env import settings
from shared.database.pool import close_pool, get_pool
from shared.errors.app_error import AppError
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
api_router.include_router(article_router, tags=["articles"])
api_router.include_router(comment_router, tags=["comments"])

app.include_router(api_router)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.add_exception_handler(AppError, app_error_handler)
app.add_exception_handler(RequestValidationError, validation_error_handler)
app.add_exception_handler(ValidationError, validation_error_handler)
app.add_exception_handler(Exception, generic_error_handler)