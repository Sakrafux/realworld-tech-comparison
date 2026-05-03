package service

import (
	"context"
	"errors"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/tests/testmocks"
	"github.com/stretchr/testify/assert"
)

func TestTagService_GetTags(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		repo := new(testmocks.MockTagRepository)
		svc := NewTagService(repo)
		ctx := context.Background()
		expectedTags := []domain.Tag{{Name: "reactjs"}, {Name: "angularjs"}}

		repo.On("FindAll", ctx).Return(expectedTags, nil)

		tags, err := svc.GetTags(ctx)

		assert.NoError(t, err)
		assert.Equal(t, expectedTags, tags)
		repo.AssertExpectations(t)
	})

	t.Run("error", func(t *testing.T) {
		repo := new(testmocks.MockTagRepository)
		svc := NewTagService(repo)
		ctx := context.Background()

		repo.On("FindAll", ctx).Return(nil, errors.New("db error"))

		_, err := svc.GetTags(ctx)

		assert.Error(t, err)
		assert.Equal(t, "db error", err.Error())
		repo.AssertExpectations(t)
	})
}
