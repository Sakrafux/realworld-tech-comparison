package web

import (
	"encoding/json"
	"net/http"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type UserHandler struct {
	userService    port.UserService
	tokenGenerator port.TokenGenerator
}

func NewUserHandler(userService port.UserService, tokenGenerator port.TokenGenerator) *UserHandler {
	return &UserHandler{
		userService:    userService,
		tokenGenerator: tokenGenerator,
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
		Email    string `json:"email"`
		Password string `json:"password"`
	} `json:"user"`
}

type registrationRequest struct {
	User struct {
		Username string `json:"username"`
		Email    string `json:"email"`
		Password string `json:"password"`
	} `json:"user"`
}

func (h *UserHandler) Register(w http.ResponseWriter, r *http.Request) {
	var req registrationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, domain.NewUnprocessableEntityError("invalid request body"))
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

	token, err := h.tokenGenerator.Generate(user)
	if err != nil {
		RespondWithError(w, domain.NewInternalError("failed to generate token"))
		return
	}

	h.respondWithUser(w, http.StatusCreated, user, token)
}

func (h *UserHandler) Login(w http.ResponseWriter, r *http.Request) {
	var req loginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, domain.NewUnprocessableEntityError("invalid request body"))
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

	token, err := h.tokenGenerator.Generate(user)
	if err != nil {
		RespondWithError(w, domain.NewInternalError("failed to generate token"))
		return
	}

	h.respondWithUser(w, http.StatusOK, user, token)
}

func (h *UserHandler) respondWithUser(w http.ResponseWriter, code int, user *domain.User, token string) {
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
