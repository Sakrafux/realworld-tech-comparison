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

func (g *jwtGenerator) Parse(tokenString string) (int64, error) {
	token, err := jwt.ParseWithClaims(tokenString, &claims{}, func(token *jwt.Token) (interface{}, error) {
		return g.secretKey, nil
	})

	if err != nil {
		return 0, err
	}

	if claims, ok := token.Claims.(*claims); ok && token.Valid {
		return claims.UserID, nil
	}

	return 0, jwt.ErrSignatureInvalid
}
