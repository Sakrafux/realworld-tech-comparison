package main

import (
	"log"
	"log/slog"
	"net/http"
	"time"

	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/web"
)

func main() {
	// httplog is designed for easy integration with a go-chi router, is based on slog and thus allows for structured logging
	logger := httplog.NewLogger("realworld", httplog.Options{
		LogLevel:        slog.LevelInfo,
		JSON:            false,
		Concise:         true,
		TimeFieldFormat: time.RFC3339,
	})

	logger.Info("Starting application")
	cfg := configuration.LoadConfig()

	db, err := configuration.NewDatabase(cfg.Database, logger)
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	router := web.NewApp(cfg, db, logger)

	logger.Info("Starting server on port " + cfg.Server.Port)
	if err := http.ListenAndServe(":"+cfg.Server.Port, router); err != nil {
		log.Fatal(err)
	}
}
