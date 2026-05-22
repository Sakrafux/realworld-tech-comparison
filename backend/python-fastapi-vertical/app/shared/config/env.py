import os


class Settings:
    DATABASE_HOST: str = os.getenv("DB_HOST", "localhost")
    DATABASE_PORT: int = int(os.getenv("DB_PORT", "5432"))
    DATABASE_USER: str = os.getenv("DB_USER", "postgres")
    DATABASE_PASSWORD: str = os.getenv("DB_PASSWORD", "password")
    DATABASE_NAME: str = os.getenv("DB_NAME", "realworld")

    JWT_SECRET: str = os.getenv("JWT_SECRET", "super-secret-jwt-key-change-in-production")
    JWT_ALGORITHM: str = os.getenv("JWT_ALGORITHM", "HS256")
    JWT_EXPIRATION_HOURS: int = int(os.getenv("JWT_EXPIRATION_HOURS", "24"))

    CORS_ALLOWED_ORIGINS: list[str] = os.getenv("CORS_ALLOWED_ORIGINS", "*").split(",")

    @property
    def database_url(self) -> str:
        return (
            f"postgresql://{self.DATABASE_USER}:{self.DATABASE_PASSWORD}"
            f"@{self.DATABASE_HOST}:{self.DATABASE_PORT}/{self.DATABASE_NAME}"
        )


settings = Settings()
