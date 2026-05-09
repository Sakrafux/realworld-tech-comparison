package web

import (
	"net/http"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/config"
	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
)

// Middlewares holds middleware functions that are passed down to cell handlers.
type Middlewares struct {
	Auth         func(http.Handler) http.Handler
	OptionalAuth func(http.Handler) http.Handler
}

func RegisterBaseMiddleware(r *chi.Mux, cfg config.WebConfig, otelCfg config.OtelConfig, logger *httplog.Logger) {
	r.Use(middleware.RequestID)
	r.Use(middleware.RealIP)
	if otelCfg.Enabled {
		r.Use(func(next http.Handler) http.Handler {
			return otelhttp.NewHandler(next, otelCfg.ServiceName)
		})
	}
	r.Use(httplog.RequestLogger(logger))
	r.Use(middleware.Recoverer)
	r.Use(middleware.Timeout(60 * time.Second))
	r.Use(middleware.Heartbeat("/health"))
	r.Use(cors.Handler(cors.Options{
		AllowedOrigins:   cfg.CorsAllowedOrigins,
		AllowedMethods:   []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type"},
		AllowCredentials: true,
		MaxAge:           300,
	}))
}
