package web

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"

	"github.com/go-chi/httplog/v2"
	"github.com/go-playground/validator/v10"
	sharedErrors "github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/errors"
)

func RespondWithError(w http.ResponseWriter, r *http.Request, err error) {
	logger := httplog.LogEntry(r.Context())

	if appErr, ok := errors.AsType[sharedErrors.AppError](err); ok {
		switch appErr.Type {
		case sharedErrors.TypeNotFound:
			respond(w, http.StatusNotFound, appErr.Message)
		case sharedErrors.TypeAlreadyExists:
			respond(w, http.StatusUnprocessableEntity, appErr.Message)
		case sharedErrors.TypeInvalidCredentials:
			respond(w, http.StatusUnauthorized, appErr.Message)
		case sharedErrors.TypeUnauthorized:
			respond(w, http.StatusUnauthorized, appErr.Message)
		case sharedErrors.TypeForbidden:
			respond(w, http.StatusForbidden, appErr.Message)
		case sharedErrors.TypeUnprocessable:
			respond(w, http.StatusUnprocessableEntity, appErr.Message)
		case sharedErrors.TypeInternal:
			logger.Error(appErr.Message)
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

	respond(w, http.StatusInternalServerError, err.Error())
}

type genericErrorResponse struct {
	Errors struct {
		Body []string `json:"body"`
	} `json:"errors"`
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
