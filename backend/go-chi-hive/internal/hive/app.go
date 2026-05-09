package hive

import (
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/httplog/v2"
	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/config"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/database"
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

	r := chi.NewRouter()
	web.RegisterBaseMiddleware(r, cfg.Web, cfg.Otel, logger)

	// Cells will be instantiated and mounted here

	r.Route("/api", func(api chi.Router) {

	})

	return &App{
		Router: r,
		DB:     db,
	}, nil
}
