package service

import (
	"context"
	"fmt"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type userService struct {
	userRepo       port.UserRepository
	passwordHasher port.PasswordHasher
}

func NewUserService(userRepo port.UserRepository, passwordHasher port.PasswordHasher) port.UserService {
	return &userService{
		userRepo:       userRepo,
		passwordHasher: passwordHasher,
	}
}

func (s *userService) Register(ctx context.Context, cmd port.RegisterCommand) (*domain.User, error) {
	// Check if user already exists by email
	existingUser, err := s.userRepo.FindByEmail(ctx, cmd.Email)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if existingUser != nil {
		return nil, domain.NewAlreadyExistsError("Email already exists")
	}

	// Check if user already exists by username
	existingUser, err = s.userRepo.FindByUsername(ctx, cmd.Username)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if existingUser != nil {
		return nil, domain.NewAlreadyExistsError("Username already exists")
	}

	// Hash password
	hashedPassword, err := s.passwordHasher.Hash(cmd.Password)
	if err != nil {
		return nil, domain.NewInternalError(fmt.Sprintf("failed to hash password: %v", err))
	}

	user := &domain.User{
		Username: cmd.Username,
		Email:    cmd.Email,
		Password: hashedPassword,
		Bio:      "",
		Image:    nil,
	}

	if err := s.userRepo.Create(ctx, user); err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return user, nil
}

func (s *userService) Login(ctx context.Context, cmd port.LoginCommand) (*domain.User, error) {
	user, err := s.userRepo.FindByEmail(ctx, cmd.Email)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, domain.NewNotFoundError("User not found")
	}

	if err := s.passwordHasher.Compare(user.Password, cmd.Password); err != nil {
		return nil, domain.NewInvalidCredentialsError("Invalid email or password")
	}

	return user, nil
}

func (s *userService) GetUser(ctx context.Context, query port.GetUserQuery) (*domain.User, error) {
	user, err := s.userRepo.FindByID(ctx, query.ID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, domain.NewResourceNotFound("User", "id", query.ID)
	}
	return user, nil
}

func (s *userService) UpdateUser(ctx context.Context, cmd port.UpdateUserCommand) (*domain.User, error) {
	user, err := s.userRepo.FindByID(ctx, cmd.ID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, domain.NewResourceNotFound("User", "id", cmd.ID)
	}

	// Check for duplicate email if it's being updated
	if cmd.Email != nil && *cmd.Email != user.Email {
		existingUser, err := s.userRepo.FindByEmail(ctx, *cmd.Email)
		if err != nil {
			return nil, domain.NewInternalError(err.Error())
		}
		if existingUser != nil {
			return nil, domain.NewAlreadyExistsError("Email already exists")
		}
	}

	// Check for duplicate username if it's being updated
	if cmd.Username != nil && *cmd.Username != user.Username {
		existingUser, err := s.userRepo.FindByUsername(ctx, *cmd.Username)
		if err != nil {
			return nil, domain.NewInternalError(err.Error())
		}
		if existingUser != nil {
			return nil, domain.NewAlreadyExistsError("Username already exists")
		}
	}

	var hashedPassword *string
	if cmd.Password != nil {
		hp, err := s.passwordHasher.Hash(*cmd.Password)
		if err != nil {
			return nil, domain.NewInternalError(fmt.Sprintf("failed to hash password: %v", err))
		}
		hashedPassword = &hp
	}

	user.Update(cmd.Username, cmd.Email, cmd.Bio, cmd.Image, hashedPassword)

	if err := s.userRepo.Update(ctx, user); err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return user, nil
}
