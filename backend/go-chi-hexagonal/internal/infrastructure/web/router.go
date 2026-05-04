package web

import (
	"log/slog"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
)

func NewRouter(cfg configuration.WebConfig, tagHandler *TagHandler, userHandler *UserHandler, profileHandler *ProfileHandler, articleHandler *ArticleHandler) *chi.Mux {
	r := chi.NewRouter()

	registerMiddleware(r, cfg)

	r.Route("/api", func(r chi.Router) {
		r.Get("/tags", tagHandler.GetTags)
		r.Post("/users", userHandler.Register)
		r.Post("/users/login", userHandler.Login)

		r.Group(func(r chi.Router) {
			r.Use(OptionalAuthMiddleware(userHandler.tokenGenerator))
			r.Get("/profiles/{username}", profileHandler.GetProfile)
		})

		r.Group(func(r chi.Router) {
			r.Use(AuthMiddleware(userHandler.tokenGenerator))
			r.Get("/user", userHandler.GetCurrentUser)
			r.Put("/user", userHandler.UpdateCurrentUser)
			r.Post("/profiles/{username}/follow", profileHandler.Follow)
			r.Delete("/profiles/{username}/follow", profileHandler.Unfollow)
			r.Post("/articles", articleHandler.CreateArticle)
		})
	})

	return r
}

func registerMiddleware(r *chi.Mux, cfg configuration.WebConfig) {
	// httplog is designed for easy integration with a go-chi router, is based on slog and thus allows for structured logging
	logger := httplog.NewLogger("realworld", httplog.Options{
		LogLevel:        slog.LevelInfo,
		JSON:            true,
		Concise:         true,
		TimeFieldFormat: time.RFC3339,
	})

	r.Use(middleware.RequestID)
	r.Use(middleware.RealIP)
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
