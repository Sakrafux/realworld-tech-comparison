package service

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type profileService struct {
	profileRepo port.ProfileRepository
	userRepo    port.UserRepository
}

// NewProfileService creates a new profile service.
func NewProfileService(profileRepo port.ProfileRepository, userRepo port.UserRepository) port.ProfileService {
	return &profileService{
		profileRepo: profileRepo,
		userRepo:    userRepo,
	}
}

func (s *profileService) GetProfile(ctx context.Context, query port.GetProfileQuery) (*domain.Profile, error) {
	profile, err := s.profileRepo.GetProfileByUsername(ctx, query.Username, query.ObserverID)
	if err != nil {
		return nil, err
	}
	if profile == nil {
		return nil, domain.NewResourceNotFound("Profile", "username", query.Username)
	}
	return profile, nil
}

func (s *profileService) FollowUser(ctx context.Context, cmd port.FollowUserCommand) (*domain.Profile, error) {
	return s.changeFollowStatus(ctx, cmd.FollowerID, cmd.Username, s.profileRepo.Follow)
}

func (s *profileService) UnfollowUser(ctx context.Context, cmd port.UnfollowUserCommand) (*domain.Profile, error) {
	return s.changeFollowStatus(ctx, cmd.FollowerID, cmd.Username, s.profileRepo.Unfollow)
}

func (s *profileService) changeFollowStatus(
	ctx context.Context,
	followerID int64,
	username string,
	action func(ctx context.Context, followerID, followedID int64) error,
) (*domain.Profile, error) {
	followedUser, err := s.userRepo.FindByUsername(ctx, username)
	if err != nil {
		return nil, err
	}
	if followedUser == nil {
		return nil, domain.NewResourceNotFound("User", "username", username)
	}

	if err := action(ctx, followerID, followedUser.ID); err != nil {
		return nil, err
	}

	return s.profileRepo.GetProfileByUsername(ctx, username, &followerID)
}
