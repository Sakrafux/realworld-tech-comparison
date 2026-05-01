package web

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"

	"github.com/go-playground/validator/v10"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

// RespondWithError maps domain errors to HTTP responses.
func RespondWithError(w http.ResponseWriter, err error) {
	if appErr, ok := errors.AsType[domain.AppError](err); ok {
		switch appErr.Type {
		case domain.TypeNotFound:
			respond(w, http.StatusNotFound, appErr.Message)
		case domain.TypeAlreadyExists:
			respond(w, http.StatusUnprocessableEntity, appErr.Message)
		case domain.TypeInvalidCredentials:
			respond(w, http.StatusUnauthorized, appErr.Message)
		case domain.TypeUnauthorized:
			respond(w, http.StatusUnauthorized, appErr.Message)
		case domain.TypeUnprocessable:
			respond(w, http.StatusUnprocessableEntity, appErr.Message)
		case domain.TypeInternal:
			respond(w, http.StatusInternalServerError, appErr.Message)
		default:
			respond(w, http.StatusInternalServerError, "An unexpected error occurred")
		}
		return
	}

	if validationErrors, ok := errors.AsType[validator.ValidationErrors](err); ok {
		msgs := make([]string, len(validationErrors))
		for i, ve := range validationErrors {
			msgs[i] = fmt.Sprintf("%s %s", ve.Field(), ve.Tag())
		}
		respondMultiple(w, http.StatusUnprocessableEntity, msgs)
		return
	}

	// Fallback for non-AppErrors
	respond(w, http.StatusInternalServerError, err.Error())
}

func respond(w http.ResponseWriter, code int, message string) {
	respondMultiple(w, code, []string{message})
}

func respondMultiple(w http.ResponseWriter, code int, messages []string) {
	var resp genericErrorResponse
	resp.Errors.Body = messages

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
