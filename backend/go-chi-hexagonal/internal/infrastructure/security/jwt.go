package security

import (
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type jwtGenerator struct {
	secretKey []byte
}

func NewJWTGenerator(secretKey string) port.TokenGenerator {
	return &jwtGenerator{
		secretKey: []byte(secretKey),
	}
}

type claims struct {
	UserID int64 `json:"user_id"`
	jwt.RegisteredClaims
}

func (g *jwtGenerator) Generate(user *domain.User) (string, error) {
	expirationTime := time.Now().Add(72 * time.Hour)
	claims := &claims{
		UserID: user.ID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expirationTime),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(g.secretKey)
}
