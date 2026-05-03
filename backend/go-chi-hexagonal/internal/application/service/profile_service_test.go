package service

import (
	"context"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/tests/testmocks"
	"github.com/stretchr/testify/assert"
)

func TestProfileService_GetProfile(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		profileRepo := new(testmocks.MockProfileRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewProfileService(profileRepo, userRepo)

		expectedProfile := &domain.Profile{Username: "testuser", Bio: "bio", Following: false}
		profileRepo.On("GetProfileByUsername", ctx, "testuser", (*int64)(nil)).Return(expectedProfile, nil)

		profile, err := svc.GetProfile(ctx, port.GetProfileQuery{Username: "testuser"})

		assert.NoError(t, err)
		assert.Equal(t, expectedProfile, profile)
		profileRepo.AssertExpectations(t)
	})

	t.Run("not found", func(t *testing.T) {
		profileRepo := new(testmocks.MockProfileRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewProfileService(profileRepo, userRepo)

		profileRepo.On("GetProfileByUsername", ctx, "nonexistent", (*int64)(nil)).Return(nil, nil)

		_, err := svc.GetProfile(ctx, port.GetProfileQuery{Username: "nonexistent"})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
	})
}

func TestProfileService_FollowUser(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		profileRepo := new(testmocks.MockProfileRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewProfileService(profileRepo, userRepo)

		followerID := int64(1)
		followedUser := &domain.User{ID: 2, Username: "user2"}
		profileRepo.On("Follow", ctx, followerID, followedUser.ID).Return(nil)
		userRepo.On("FindByUsername", ctx, "user2").Return(followedUser, nil)

		expectedProfile := &domain.Profile{Username: "user2", Following: true}
		profileRepo.On("GetProfileByUsername", ctx, "user2", &followerID).Return(expectedProfile, nil)

		profile, err := svc.FollowUser(ctx, port.FollowUserCommand{FollowerID: followerID, Username: "user2"})

		assert.NoError(t, err)
		assert.Equal(t, expectedProfile, profile)
		profileRepo.AssertExpectations(t)
		userRepo.AssertExpectations(t)
	})

	t.Run("user to follow not found", func(t *testing.T) {
		profileRepo := new(testmocks.MockProfileRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewProfileService(profileRepo, userRepo)

		userRepo.On("FindByUsername", ctx, "nonexistent").Return(nil, nil)

		_, err := svc.FollowUser(ctx, port.FollowUserCommand{FollowerID: 1, Username: "nonexistent"})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
	})
}

func TestProfileService_UnfollowUser(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		profileRepo := new(testmocks.MockProfileRepository)
		userRepo := new(testmocks.MockUserRepository)
		svc := NewProfileService(profileRepo, userRepo)

		followerID := int64(1)
		followedUser := &domain.User{ID: 2, Username: "user2"}
		profileRepo.On("Unfollow", ctx, followerID, followedUser.ID).Return(nil)
		userRepo.On("FindByUsername", ctx, "user2").Return(followedUser, nil)

		expectedProfile := &domain.Profile{Username: "user2", Following: false}
		profileRepo.On("GetProfileByUsername", ctx, "user2", &followerID).Return(expectedProfile, nil)

		profile, err := svc.UnfollowUser(ctx, port.UnfollowUserCommand{FollowerID: followerID, Username: "user2"})

		assert.NoError(t, err)
		assert.Equal(t, expectedProfile, profile)
		profileRepo.AssertExpectations(t)
		userRepo.AssertExpectations(t)
	})
}
