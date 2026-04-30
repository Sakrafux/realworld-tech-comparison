package web

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

// Mock for TagService
type MockTagService struct {
	mock.Mock
}

func (m *MockTagService) GetTags(ctx context.Context) ([]domain.Tag, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]domain.Tag), args.Error(1)
}

func TestTagHandler_GetTags(t *testing.T) {
	t.Run("success", func(t *testing.T) {
		tags := []domain.Tag{{Name: "reactjs"}, {Name: "angularjs"}}
		svc := new(MockTagService)
		h := NewTagHandler(svc)

		svc.On("GetTags", mock.Anything).Return(tags, nil)

		req := httptest.NewRequest("GET", "/api/tags", nil)
		w := httptest.NewRecorder()

		h.GetTags(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		assert.Equal(t, "application/json", w.Header().Get("Content-Type"))

		var resp tagsResponse
		err := json.NewDecoder(w.Body).Decode(&resp)
		assert.NoError(t, err)
		assert.Equal(t, []string{"reactjs", "angularjs"}, resp.Tags)
		svc.AssertExpectations(t)
	})

	t.Run("error", func(t *testing.T) {
		svc := new(MockTagService)
		h := NewTagHandler(svc)

		svc.On("GetTags", mock.Anything).Return(nil, errors.New("service error"))

		req := httptest.NewRequest("GET", "/api/tags", nil)
		w := httptest.NewRecorder()

		h.GetTags(w, req)

		assert.Equal(t, http.StatusInternalServerError, w.Code)
		svc.AssertExpectations(t)
	})
}
