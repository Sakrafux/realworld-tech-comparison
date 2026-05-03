package port

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type RegisterCommand struct {
	Username string
	Email    string
	Password string
}

type LoginCommand struct {
	Email    string
	Password string
}

type UpdateUserCommand struct {
	ID       int64
	Username *string
	Email    *string
	Password *string
	Bio      *string
	Image    *string
}

type GetUserQuery struct {
	ID int64
}

// UserService defines the inbound port for user-related use cases.
type UserService interface {
	Register(ctx context.Context, cmd RegisterCommand) (*domain.User, error)
	Login(ctx context.Context, cmd LoginCommand) (*domain.User, error)
	GetUser(ctx context.Context, query GetUserQuery) (*domain.User, error)
	UpdateUser(ctx context.Context, cmd UpdateUserCommand) (*domain.User, error)
}

// UserRepository defines the outbound port for user data persistence.
type UserRepository interface {
	Create(ctx context.Context, user *domain.User) error
	FindByEmail(ctx context.Context, email string) (*domain.User, error)
	FindByUsername(ctx context.Context, username string) (*domain.User, error)
	FindByID(ctx context.Context, id int64) (*domain.User, error)
	Update(ctx context.Context, user *domain.User) error
}

// PasswordHasher defines the outbound port for password security.
type PasswordHasher interface {
	Hash(password string) (string, error)
	Compare(hashedPassword, password string) error
}

// TokenGenerator defines the outbound port for generating and parsing authentication tokens.
type TokenGenerator interface {
	Generate(user *domain.User) (string, error)
	Parse(token string) (int64, error)
}
