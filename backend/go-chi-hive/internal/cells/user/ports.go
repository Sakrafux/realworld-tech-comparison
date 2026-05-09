package user

import (
	"context"
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

// Service defines the inbound port for user-related use cases.
type Service interface {
	Register(ctx context.Context, cmd RegisterCommand) (*User, error)
	Login(ctx context.Context, cmd LoginCommand) (*User, error)
	GetUser(ctx context.Context, id int64) (*User, error)
	UpdateUser(ctx context.Context, cmd UpdateUserCommand) (*User, error)

	GetProfile(ctx context.Context, username string, observerID *int64) (*Profile, error)
	FollowUser(ctx context.Context, followerID int64, username string) (*Profile, error)
	UnfollowUser(ctx context.Context, followerID int64, username string) (*Profile, error)
}

// Repository defines the outbound port for user data persistence.
type Repository interface {
	Create(ctx context.Context, user *User) error
	FindByEmail(ctx context.Context, email string) (*User, error)
	FindByUsername(ctx context.Context, username string) (*User, error)
	FindByID(ctx context.Context, id int64) (*User, error)
	Update(ctx context.Context, user *User) error

	GetProfileByUsername(ctx context.Context, username string, observerID *int64) (*Profile, error)
	Follow(ctx context.Context, followerID, followedID int64) error
	Unfollow(ctx context.Context, followerID, followedID int64) error
}

// PasswordHasher defines the outbound port for password security.
type PasswordHasher interface {
	Hash(password string) (string, error)
	Compare(hashedPassword, password string) error
}

// TokenGenerator defines the outbound port for generating tokens.
type TokenGenerator interface {
	Generate(userID int64) (string, error)
}
