package web

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/stretchr/testify/assert"
)

func TestRespondWithError(t *testing.T) {
	tests := []struct {
		name         string
		err          error
		expectedCode int
		expectedBody []string
	}{
		{
			name:         "NotFound error",
			err:          domain.NewNotFoundError("not found"),
			expectedCode: http.StatusNotFound,
			expectedBody: []string{"not found"},
		},
		{
			name:         "AlreadyExists error",
			err:          domain.NewAlreadyExistsError("exists"),
			expectedCode: http.StatusUnprocessableEntity,
			expectedBody: []string{"exists"},
		},
		{
			name:         "Unauthorized error",
			err:          domain.NewUnauthorizedError("unauthorized"),
			expectedCode: http.StatusUnauthorized,
			expectedBody: []string{"unauthorized"},
		},
		{
			name:         "InvalidCredentials error",
			err:          domain.NewInvalidCredentialsError("invalid creds"),
			expectedCode: http.StatusUnauthorized,
			expectedBody: []string{"invalid creds"},
		},
		{
			name:         "UnprocessableEntity error",
			err:          domain.NewUnprocessableEntityError("unprocessable"),
			expectedCode: http.StatusUnprocessableEntity,
			expectedBody: []string{"unprocessable"},
		},
		{
			name:         "Internal error",
			err:          domain.NewInternalError("internal"),
			expectedCode: http.StatusInternalServerError,
			expectedBody: []string{"internal"},
		},
		{
			name:         "Generic error",
			err:          errors.New("generic"),
			expectedCode: http.StatusInternalServerError,
			expectedBody: []string{"generic"},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			w := httptest.NewRecorder()
			r := httptest.NewRequest("GET", "/", nil)
			RespondWithError(w, r, tt.err)

			assert.Equal(t, tt.expectedCode, w.Code)
			var resp genericErrorResponse
			err := json.NewDecoder(w.Body).Decode(&resp)
			assert.NoError(t, err)
			assert.Equal(t, tt.expectedBody, resp.Errors.Body)
		})
	}
}
