package web

import (
	"context"
	"net/http"
	"strings"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/errors"
)

type contextKey string

const userIDKey contextKey = "user_id"

type TokenParser interface {
	Parse(token string) (int64, error)
}

func AuthMiddleware(tokenParser TokenParser) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				RespondWithError(w, r, errors.NewUnauthorizedError("authorization header is required"))
				return
			}

			parts := strings.Split(authHeader, " ")
			if len(parts) != 2 || parts[0] != "Token" {
				RespondWithError(w, r, errors.NewUnauthorizedError("invalid authorization header format. Expected 'Token <token>'"))
				return
			}

			userID, err := tokenParser.Parse(parts[1])
			if err != nil {
				RespondWithError(w, r, errors.NewUnauthorizedError("invalid or expired token"))
				return
			}

			ctx := context.WithValue(r.Context(), userIDKey, userID)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}

func OptionalAuthMiddleware(tokenParser TokenParser) func(http.Handler) http.Handler {
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
			userID, err := tokenParser.Parse(token)
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

func WithUserID(ctx context.Context, userID int64) context.Context {
	return context.WithValue(ctx, userIDKey, userID)
}
