package security

import (
	"time"

	"github.com/golang-jwt/jwt/v5"
)

type JWTGenerator struct {
	secretKey []byte
}

func NewJWTGenerator(secretKey string) *JWTGenerator {
	return &JWTGenerator{
		secretKey: []byte(secretKey),
	}
}

type claims struct {
	UserID int64 `json:"user_id"`
	jwt.RegisteredClaims
}

func (g *JWTGenerator) Generate(userID int64) (string, error) {
	expirationTime := time.Now().Add(72 * time.Hour)
	c := &claims{
		UserID: userID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expirationTime),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, c)
	return token.SignedString(g.secretKey)
}

func (g *JWTGenerator) Parse(tokenString string) (int64, error) {
	token, err := jwt.ParseWithClaims(tokenString, &claims{}, func(token *jwt.Token) (any, error) {
		return g.secretKey, nil
	})

	if err != nil {
		return 0, err
	}

	if c, ok := token.Claims.(*claims); ok && token.Valid {
		return c.UserID, nil
	}

	return 0, jwt.ErrSignatureInvalid
}
