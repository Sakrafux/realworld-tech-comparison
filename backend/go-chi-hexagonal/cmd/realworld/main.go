package main

import (
	"log"
	"log/slog"
	"net/http"

	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/service"
	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/persistence"
	"github.com/Sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/web"
)

func main() {
	slog.Info("Starting application")
	cfg := configuration.LoadConfig()

	db, err := configuration.NewDatabase(cfg.Database)
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	tagRepo := persistence.NewPostgresTagRepository(db)
	tagService := service.NewTagService(tagRepo)
	tagHandler := web.NewTagHandler(tagService)

	router := web.NewRouter(cfg.Web, tagHandler)

	slog.Info("Starting server on port " + cfg.Server.Port)
	if err := http.ListenAndServe(":"+cfg.Server.Port, router); err != nil {
		log.Fatal(err)
	}
}
