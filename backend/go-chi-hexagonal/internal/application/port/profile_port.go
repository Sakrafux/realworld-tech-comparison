package port

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type GetProfileQuery struct {
	Username   string
	ObserverID *int64
}

type FollowUserCommand struct {
	FollowerID int64
	Username   string
}

type UnfollowUserCommand struct {
	FollowerID int64
	Username   string
}

// ProfileService defines the inbound port for profile-related use cases.
type ProfileService interface {
	GetProfile(ctx context.Context, query GetProfileQuery) (*domain.Profile, error)
	FollowUser(ctx context.Context, cmd FollowUserCommand) (*domain.Profile, error)
	UnfollowUser(ctx context.Context, cmd UnfollowUserCommand) (*domain.Profile, error)
}

// ProfileRepository defines the outbound port for profile data persistence.
type ProfileRepository interface {
	GetProfileByUsername(ctx context.Context, username string, observerID *int64) (*domain.Profile, error)
	Follow(ctx context.Context, followerID, followedID int64) error
	Unfollow(ctx context.Context, followerID, followedID int64) error
}
