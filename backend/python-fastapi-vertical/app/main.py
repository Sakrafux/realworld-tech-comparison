from contextlib import asynccontextmanager

from fastapi import FastAPI, APIRouter
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
from pydantic import ValidationError

from features.article.controller import router as article_router
from features.comment.controller import router as comment_router
from features.user.controller import router as user_router
from shared.config.env import settings
from shared.config.otel import init_otel, shutdown_otel
from shared.database.pool import close_pool, get_pool
from shared.errors.app_error import AppError
from shared.web.error_handler import app_error_handler, generic_error_handler, validation_error_handler

meter_provider = init_otel()


@asynccontextmanager
async def lifespan(app: FastAPI):
    await get_pool()
    yield
    await close_pool()
    shutdown_otel(meter_provider)


app = FastAPI(
    title="RealWorld Conduit API",
    description="Conduit API documentation",
    version="1.0.0",
    lifespan=lifespan,
)

# 1. Add Routers
api_router = APIRouter(prefix="/api")
api_router.include_router(user_router, tags=["users"])
api_router.include_router(article_router, tags=["articles"])
api_router.include_router(comment_router, tags=["comments"])
app.include_router(api_router)

# 2. Add Middlewares
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 3. Add Exception Handlers
app.add_exception_handler(AppError, app_error_handler)
app.add_exception_handler(RequestValidationError, validation_error_handler)
app.add_exception_handler(ValidationError, validation_error_handler)
app.add_exception_handler(Exception, generic_error_handler)

# 4. Instrument the App LAST
FastAPIInstrumentor().instrument_app(app)