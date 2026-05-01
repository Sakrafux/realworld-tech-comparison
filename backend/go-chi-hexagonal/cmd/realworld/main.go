package main

import (
	"log"
	"log/slog"
	"net/http"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/web"
)

func main() {
	slog.Info("Starting application")
	cfg := configuration.LoadConfig()

	db, err := configuration.NewDatabase(cfg.Database)
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	router := web.NewApp(cfg, db)

	slog.Info("Starting server on port " + cfg.Server.Port)
	if err := http.ListenAndServe(":"+cfg.Server.Port, router); err != nil {
		log.Fatal(err)
	}
}
