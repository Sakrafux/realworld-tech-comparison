package service

import (
	"context"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/tests/testmocks"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestUserService_Register(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		hasher := new(testmocks.MockPasswordHasher)
		svc := NewUserService(repo, hasher)
		cmd := port.RegisterCommand{Username: "test", Email: "test@test.com", Password: "pass"}
		repo.On("FindByEmail", ctx, cmd.Email).Return(nil, nil)
		repo.On("FindByUsername", ctx, cmd.Username).Return(nil, nil)
		hasher.On("Hash", cmd.Password).Return("hashed", nil)
		repo.On("Create", ctx, mock.AnythingOfType("*domain.User")).Return(nil)

		user, err := svc.Register(ctx, cmd)

		assert.NoError(t, err)
		assert.Equal(t, cmd.Username, user.Username)
		repo.AssertExpectations(t)
		hasher.AssertExpectations(t)
	})

	t.Run("already exists by email", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		svc := NewUserService(repo, nil)
		cmd := port.RegisterCommand{Email: "exists@test.com"}
		repo.On("FindByEmail", ctx, cmd.Email).Return(&domain.User{}, nil)

		_, err := svc.Register(ctx, cmd)

		assert.Error(t, err)
		assert.Equal(t, domain.TypeAlreadyExists, err.(domain.AppError).Type)
	})
}

func TestUserService_Login(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		hasher := new(testmocks.MockPasswordHasher)
		svc := NewUserService(repo, hasher)
		cmd := port.LoginCommand{Email: "test@test.com", Password: "pass"}
		user := &domain.User{Email: cmd.Email, Password: "hashed"}
		repo.On("FindByEmail", ctx, cmd.Email).Return(user, nil)
		hasher.On("Compare", user.Password, cmd.Password).Return(nil)

		res, err := svc.Login(ctx, cmd)

		assert.NoError(t, err)
		assert.Equal(t, user, res)
		repo.AssertExpectations(t)
		hasher.AssertExpectations(t)
	})

	t.Run("invalid credentials", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		svc := NewUserService(repo, nil)
		cmd := port.LoginCommand{Email: "test@test.com", Password: "wrong"}
		repo.On("FindByEmail", ctx, cmd.Email).Return(nil, nil)

		_, err := svc.Login(ctx, cmd)

		assert.Error(t, err)
		assert.Equal(t, domain.TypeInvalidCredentials, err.(domain.AppError).Type)
	})
}

func TestUserService_GetUser(t *testing.T) {
	ctx := context.Background()

	t.Run("success", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		svc := NewUserService(repo, nil)
		expectedUser := &domain.User{ID: 1, Username: "testuser"}
		repo.On("FindByID", ctx, int64(1)).Return(expectedUser, nil)

		user, err := svc.GetUser(ctx, port.GetUserQuery{ID: 1})

		assert.NoError(t, err)
		assert.Equal(t, expectedUser, user)
		repo.AssertExpectations(t)
	})

	t.Run("not found", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		svc := NewUserService(repo, nil)
		repo.On("FindByID", ctx, int64(2)).Return(nil, nil)

		_, err := svc.GetUser(ctx, port.GetUserQuery{ID: 2})

		assert.Error(t, err)
		assert.IsType(t, domain.AppError{}, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
		repo.AssertExpectations(t)
	})
}

func TestUserService_UpdateUser(t *testing.T) {
	ctx := context.Background()

	t.Run("success all fields", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		hasher := new(testmocks.MockPasswordHasher)
		svc := NewUserService(repo, hasher)
		existingUser := &domain.User{ID: 1, Username: "old", Email: "old@old.com", Password: "oldhash"}
		repo.On("FindByID", ctx, int64(1)).Return(existingUser, nil)

		newUsername := "new"
		newEmail := "new@new.com"
		newBio := "new bio"
		newImage := "http://img.com"
		newPassword := "newpass"
		newHash := "newhash"

		hasher.On("Hash", newPassword).Return(newHash, nil)
		repo.On("Update", ctx, mock.MatchedBy(func(u *domain.User) bool {
			return u.Username == newUsername && u.Email == newEmail && u.Bio == newBio && *u.Image == newImage && u.Password == newHash
		})).Return(nil)

		user, err := svc.UpdateUser(ctx, port.UpdateUserCommand{
			ID:       1,
			Username: &newUsername,
			Email:    &newEmail,
			Bio:      &newBio,
			Image:    &newImage,
			Password: &newPassword,
		})

		assert.NoError(t, err)
		assert.Equal(t, newUsername, user.Username)
		repo.AssertExpectations(t)
		hasher.AssertExpectations(t)
	})

	t.Run("not found", func(t *testing.T) {
		repo := new(testmocks.MockUserRepository)
		svc := NewUserService(repo, nil)
		repo.On("FindByID", ctx, int64(2)).Return(nil, nil)

		_, err := svc.UpdateUser(ctx, port.UpdateUserCommand{ID: 2})

		assert.Error(t, err)
		assert.Equal(t, domain.TypeNotFound, err.(domain.AppError).Type)
		repo.AssertExpectations(t)
	})
}
