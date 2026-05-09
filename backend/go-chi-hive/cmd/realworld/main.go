package main

import (
	"context"
	"log"
	"log/slog"
	"net/http"
	"time"

	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/hive"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/config"
)

func main() {
	logger := httplog.NewLogger("realworld", httplog.Options{
		LogLevel:        slog.LevelInfo,
		JSON:            false,
		Concise:         true,
		TimeFieldFormat: time.RFC3339,
	})

	logger.Info("Starting application")
	cfg := config.LoadConfig()

	ctx := context.Background()
	shutdown, err := config.InitOtel(ctx, cfg.Otel)
	if err != nil {
		log.Fatal(err)
	}
	defer func() {
		if err := shutdown(ctx); err != nil {
			log.Fatal(err)
		}
	}()

	app, err := hive.NewApp(cfg, logger)
	if err != nil {
		log.Fatal(err)
	}
	defer app.DB.Close()

	logger.Info("Starting server on port " + cfg.Server.Port)
	if err := http.ListenAndServe(":"+cfg.Server.Port, app.Router); err != nil {
		log.Fatal(err)
	}
}
