package web

import (
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/httplog/v2"
	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/service"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/configuration"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/persistence"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/infrastructure/security"
)

// NewApp wires all dependencies and returns a configured router.
// This is used by both the main application and integration tests.
func NewApp(cfg *configuration.Config, db *sqlx.DB, logger *httplog.Logger) *chi.Mux {
	// Tags
	tagRepo := persistence.NewTagRepository(db)
	tagService := service.NewTagService(tagRepo)
	tagHandler := NewTagHandler(tagService)

	// User & Auth
	passwordHasher := security.NewBcryptHasher()
	tokenGenerator := security.NewJWTGenerator(cfg.Security.JWTSecret)
	userRepo := persistence.NewUserRepository(db)
	userService := service.NewUserService(userRepo, passwordHasher)
	userHandler := NewUserHandler(userService, tokenGenerator)

	// Profiles
	profileRepo := persistence.NewProfileRepository(db)
	profileService := service.NewProfileService(profileRepo, userRepo)
	profileHandler := NewProfileHandler(profileService)

	// Articles
	articleRepo := persistence.NewArticleRepository(db)
	articleService := service.NewArticleService(articleRepo, userRepo)
	articleHandler := NewArticleHandler(articleService)

	// Comments
	commentRepo := persistence.NewCommentRepository(db)
	commentService := service.NewCommentService(commentRepo, articleRepo, userRepo)
	commentHandler := NewCommentHandler(commentService)

	return NewRouter(cfg.Web, logger, tagHandler, userHandler, profileHandler, articleHandler, commentHandler)
}
