package web

import (
	"encoding/json"
	"net/http"
	"reflect"
	"strings"

	"github.com/go-playground/validator/v10"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type UserHandler struct {
	userService    port.UserService
	tokenGenerator port.TokenGenerator
	validate       *validator.Validate
}

func NewUserHandler(userService port.UserService, tokenGenerator port.TokenGenerator) *UserHandler {
	v := validator.New()

	// Register function to use JSON tag names in validation errors
	v.RegisterTagNameFunc(func(fld reflect.StructField) string {
		name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
		if name == "-" {
			return ""
		}
		return name
	})

	return &UserHandler{
		userService:    userService,
		tokenGenerator: tokenGenerator,
		validate:       v,
	}
}

type userResponse struct {
	User struct {
		Email    string  `json:"email"`
		Token    string  `json:"token"`
		Username string  `json:"username"`
		Bio      string  `json:"bio"`
		Image    *string `json:"image"`
	} `json:"user"`
}

type genericErrorResponse struct {
	Errors struct {
		Body []string `json:"body"`
	} `json:"errors"`
}

type loginRequest struct {
	User struct {
		Email    string `json:"email" validate:"required,email"`
		Password string `json:"password" validate:"required"`
	} `json:"user" validate:"required"`
}

type registrationRequest struct {
	User struct {
		Username string `json:"username" validate:"required,min=3,max=50"`
		Email    string `json:"email" validate:"required,email,max=100"`
		Password string `json:"password" validate:"required,min=8,max=60"`
	} `json:"user" validate:"required"`
}

type updateUserRequest struct {
	User struct {
		Username *string `json:"username" validate:"omitempty,min=3,max=50"`
		Email    *string `json:"email" validate:"omitempty,email,max=100"`
		Password *string `json:"password" validate:"omitempty,min=8,max=60"`
		Bio      *string `json:"bio"`
		Image    *string `json:"image"`
	} `json:"user" validate:"required"`
}

func (h *UserHandler) Register(w http.ResponseWriter, r *http.Request) {
	var req registrationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, domain.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		RespondWithError(w, err)
		return
	}

	user, err := h.userService.Register(r.Context(), port.RegisterCommand{
		Username: req.User.Username,
		Email:    req.User.Email,
		Password: req.User.Password,
	})
	if err != nil {
		RespondWithError(w, err)
		return
	}

	h.respondWithUser(w, http.StatusCreated, user)
}

func (h *UserHandler) Login(w http.ResponseWriter, r *http.Request) {
	var req loginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, domain.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		RespondWithError(w, err)
		return
	}

	user, err := h.userService.Login(r.Context(), port.LoginCommand{
		Email:    req.User.Email,
		Password: req.User.Password,
	})
	if err != nil {
		RespondWithError(w, err)
		return
	}

	h.respondWithUser(w, http.StatusOK, user)
}

func (h *UserHandler) GetCurrentUser(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	user, err := h.userService.GetUser(r.Context(), port.GetUserQuery{ID: userID})
	if err != nil {
		RespondWithError(w, err)
		return
	}

	h.respondWithUser(w, http.StatusOK, user)
}

func (h *UserHandler) UpdateCurrentUser(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	var req updateUserRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, domain.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		RespondWithError(w, err)
		return
	}

	user, err := h.userService.UpdateUser(r.Context(), port.UpdateUserCommand{
		ID:       userID,
		Username: req.User.Username,
		Email:    req.User.Email,
		Password: req.User.Password,
		Bio:      req.User.Bio,
		Image:    req.User.Image,
	})
	if err != nil {
		RespondWithError(w, err)
		return
	}

	h.respondWithUser(w, http.StatusOK, user)
}

func (h *UserHandler) respondWithUser(w http.ResponseWriter, code int, user *domain.User) {
	token, err := h.tokenGenerator.Generate(user)
	if err != nil {
		RespondWithError(w, domain.NewInternalError("failed to generate token"))
		return
	}

	var resp userResponse
	resp.User.Email = user.Email
	resp.User.Token = token
	resp.User.Username = user.Username
	resp.User.Bio = user.Bio
	resp.User.Image = user.Image

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
