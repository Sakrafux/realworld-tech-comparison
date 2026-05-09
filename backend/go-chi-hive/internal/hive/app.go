package hive

import (
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/httplog/v2"
	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/cells/user"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/config"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/database"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/security"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/web"
)

type App struct {
	Router *chi.Mux
	DB     *sqlx.DB
}

func NewApp(cfg *config.Config, logger *httplog.Logger) (*App, error) {
	db, err := database.NewDatabase(cfg.Database, logger)
	if err != nil {
		return nil, err
	}

	tokenGenerator := security.NewJWTGenerator(cfg.Security.JWTSecret)
	passwordHasher := security.NewBcryptHasher()

	middlewares := web.Middlewares{
		Auth:         web.AuthMiddleware(tokenGenerator),
		OptionalAuth: web.OptionalAuthMiddleware(tokenGenerator),
	}

	// 2. Init Cells
	userRepo := user.NewRepository(db)
	userSvc := user.NewService(userRepo, passwordHasher)
	userHandler := user.NewHandler(userSvc, tokenGenerator)

	r := chi.NewRouter()
	web.RegisterBaseMiddleware(r, cfg.Web, cfg.Otel, logger)

	r.Route("/api", func(api chi.Router) {
		userHandler.MountRoutes(api, middlewares)
	})

	return &App{
		Router: r,
		DB:     db,
	}, nil
}
