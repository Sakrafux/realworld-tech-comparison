package web

import (
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
	"github.com/go-chi/httplog/v2"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
)

func NewRouter(
	cfg configuration.WebConfig,
	logger *httplog.Logger,
	tagHandler *TagHandler,
	userHandler *UserHandler,
	profileHandler *ProfileHandler,
	articleHandler *ArticleHandler,
	commentHandler *CommentHandler,
) *chi.Mux {
	r := chi.NewRouter()

	registerMiddleware(r, cfg, logger)

	r.Route("/api", func(r chi.Router) {
		r.Get("/tags", tagHandler.GetTags)
		r.Post("/users", userHandler.Register)
		r.Post("/users/login", userHandler.Login)

		r.Group(func(r chi.Router) {
			r.Use(OptionalAuthMiddleware(userHandler.tokenGenerator))
			r.Get("/profiles/{username}", profileHandler.GetProfile)
			r.Get("/articles/{slug}", articleHandler.GetArticle)
			r.Get("/articles/{slug}/comments", commentHandler.GetComments)
		})

		r.Group(func(r chi.Router) {
			r.Use(AuthMiddleware(userHandler.tokenGenerator))
			r.Get("/user", userHandler.GetCurrentUser)
			r.Put("/user", userHandler.UpdateCurrentUser)
			r.Post("/profiles/{username}/follow", profileHandler.Follow)
			r.Delete("/profiles/{username}/follow", profileHandler.Unfollow)
			r.Get("/articles/feed", articleHandler.GetFeed)
			r.Post("/articles", articleHandler.CreateArticle)
			r.Put("/articles/{slug}", articleHandler.UpdateArticle)
			r.Delete("/articles/{slug}", articleHandler.DeleteArticle)
			r.Post("/articles/{slug}/favorite", articleHandler.FavoriteArticle)
			r.Delete("/articles/{slug}/favorite", articleHandler.UnfavoriteArticle)
			r.Post("/articles/{slug}/comments", commentHandler.CreateComment)
			r.Delete("/articles/{slug}/comments/{id}", commentHandler.DeleteComment)
		})
	})

	return r
}

func registerMiddleware(r *chi.Mux, cfg configuration.WebConfig, logger *httplog.Logger) {
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
