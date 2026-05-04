package web

import (
	"context"
	"net/http"
	"strings"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type contextKey string

const userIDKey contextKey = "user_id"

// AuthMiddleware attempts to authenticate the user and returns with error if no token or an invalid token is provided.
func AuthMiddleware(tokenGenerator port.TokenGenerator) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				RespondWithError(w, r, domain.NewUnauthorizedError("authorization header is required"))
				return
			}

			parts := strings.Split(authHeader, " ")
			if len(parts) != 2 || parts[0] != "Token" {
				RespondWithError(w, r, domain.NewUnauthorizedError("invalid authorization header format. Expected 'Token <token>'"))
				return
			}

			userID, err := tokenGenerator.Parse(parts[1])
			if err != nil {
				RespondWithError(w, r, domain.NewUnauthorizedError("invalid or expired token"))
				return
			}

			ctx := context.WithValue(r.Context(), userIDKey, userID)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

// OptionalAuthMiddleware attempts to authenticate the user but proceeds even if no token or an invalid token is provided.
func OptionalAuthMiddleware(tokenGenerator port.TokenGenerator) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				next.ServeHTTP(w, r)
				return
			}

			parts := strings.Split(authHeader, " ")
			if len(parts) != 2 || parts[0] != "Token" {
				next.ServeHTTP(w, r)
				return
			}

			token := parts[1]
			userID, err := tokenGenerator.Parse(token)
			if err != nil {
				next.ServeHTTP(w, r)
				return
			}

			ctx := context.WithValue(r.Context(), userIDKey, userID)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func GetUserIDFromContext(ctx context.Context) (int64, bool) {
	userID, ok := ctx.Value(userIDKey).(int64)
	return userID, ok
}
