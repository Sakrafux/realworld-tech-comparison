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

func AuthMiddleware(tokenGenerator port.TokenGenerator) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := r.Header.Get("Authorization")
			if authHeader == "" {
				RespondWithError(w, domain.NewUnauthorizedError("authorization header is required"))
				return
			}

			parts := strings.Split(authHeader, " ")
			if len(parts) != 2 || parts[0] != "Token" {
				RespondWithError(w, domain.NewUnauthorizedError("invalid authorization header format. Expected 'Token <token>'"))
				return
			}

			token := parts[1]
			userID, err := tokenGenerator.Parse(token)
			if err != nil {
				RespondWithError(w, domain.NewUnauthorizedError("invalid or expired token"))
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
