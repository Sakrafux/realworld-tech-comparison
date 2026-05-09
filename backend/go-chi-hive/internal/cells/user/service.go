package user

import (
	"context"
	"fmt"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/errors"
)

type service struct {
	repo           Repository
	passwordHasher PasswordHasher
}

func NewService(repo Repository, passwordHasher PasswordHasher) Service {
	return &service{
		repo:           repo,
		passwordHasher: passwordHasher,
	}
}

func (s *service) Register(ctx context.Context, cmd RegisterCommand) (*User, error) {
	existingUser, err := s.repo.FindByEmail(ctx, cmd.Email)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if existingUser != nil {
		return nil, errors.NewAlreadyExistsError("Email already exists")
	}

	existingUser, err = s.repo.FindByUsername(ctx, cmd.Username)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if existingUser != nil {
		return nil, errors.NewAlreadyExistsError("Username already exists")
	}

	hashedPassword, err := s.passwordHasher.Hash(cmd.Password)
	if err != nil {
		return nil, errors.NewInternalError(fmt.Sprintf("failed to hash password: %v", err))
	}

	user := &User{
		Username: cmd.Username,
		Email:    cmd.Email,
		Password: hashedPassword,
		Bio:      "",
		Image:    nil,
	}

	if err := s.repo.Create(ctx, user); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return user, nil
}

func (s *service) Login(ctx context.Context, cmd LoginCommand) (*User, error) {
	user, err := s.repo.FindByEmail(ctx, cmd.Email)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, errors.NewNotFoundError("User not found")
	}

	if err := s.passwordHasher.Compare(user.Password, cmd.Password); err != nil {
		return nil, errors.NewInvalidCredentialsError("Invalid email or password")
	}

	return user, nil
}

func (s *service) GetUser(ctx context.Context, id int64) (*User, error) {
	user, err := s.repo.FindByID(ctx, id)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, errors.NewResourceNotFound("User", "id", id)
	}
	return user, nil
}

func (s *service) GetUserByUsername(ctx context.Context, username string) (*User, error) {
	user, err := s.repo.FindByUsername(ctx, username)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, errors.NewResourceNotFound("User", "username", username)
	}
	return user, nil
}

func (s *service) UpdateUser(ctx context.Context, cmd UpdateUserCommand) (*User, error) {
	user, err := s.repo.FindByID(ctx, cmd.ID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, errors.NewResourceNotFound("User", "id", cmd.ID)
	}

	if cmd.Email != nil && *cmd.Email != user.Email {
		existingUser, err := s.repo.FindByEmail(ctx, *cmd.Email)
		if err != nil {
			return nil, errors.NewInternalError(err.Error())
		}
		if existingUser != nil {
			return nil, errors.NewAlreadyExistsError("Email already exists")
		}
	}

	if cmd.Username != nil && *cmd.Username != user.Username {
		existingUser, err := s.repo.FindByUsername(ctx, *cmd.Username)
		if err != nil {
			return nil, errors.NewInternalError(err.Error())
		}
		if existingUser != nil {
			return nil, errors.NewAlreadyExistsError("Username already exists")
		}
	}

	var hashedPassword *string
	if cmd.Password != nil {
		hp, err := s.passwordHasher.Hash(*cmd.Password)
		if err != nil {
			return nil, errors.NewInternalError(fmt.Sprintf("failed to hash password: %v", err))
		}
		hashedPassword = &hp
	}

	user.Update(cmd.Username, cmd.Email, cmd.Bio, cmd.Image, hashedPassword)

	if err := s.repo.Update(ctx, user); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return user, nil
}

func (s *service) GetProfile(ctx context.Context, username string, observerID *int64) (*Profile, error) {
	profile, err := s.repo.GetProfileByUsername(ctx, username, observerID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if profile == nil {
		return nil, errors.NewResourceNotFound("Profile", "username", username)
	}
	return profile, nil
}

func (s *service) FollowUser(ctx context.Context, followerID int64, username string) (*Profile, error) {
	return s.changeFollowStatus(ctx, followerID, username, s.repo.Follow)
}

func (s *service) UnfollowUser(ctx context.Context, followerID int64, username string) (*Profile, error) {
	return s.changeFollowStatus(ctx, followerID, username, s.repo.Unfollow)
}

func (s *service) changeFollowStatus(
	ctx context.Context,
	followerID int64,
	username string,
	action func(ctx context.Context, followerID, followedID int64) error,
) (*Profile, error) {
	followedUser, err := s.repo.FindByUsername(ctx, username)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}
	if followedUser == nil {
		return nil, errors.NewResourceNotFound("User", "username", username)
	}

	if err := action(ctx, followerID, followedUser.ID); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return s.repo.GetProfileByUsername(ctx, username, &followerID)
}
