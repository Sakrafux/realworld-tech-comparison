package user

import (
	"encoding/json"
	"net/http"
	"reflect"
	"strings"

	"github.com/go-chi/chi/v5"
	"github.com/go-playground/validator/v10"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/errors"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/web"
)

type Handler struct {
	service        Service
	tokenGenerator TokenGenerator
	validate       *validator.Validate
}

func NewHandler(service Service, tokenGenerator TokenGenerator) *Handler {
	v := validator.New()

	v.RegisterTagNameFunc(func(fld reflect.StructField) string {
		name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
		if name == "-" {
			return ""
		}
		return name
	})

	return &Handler{
		service:        service,
		tokenGenerator: tokenGenerator,
		validate:       v,
	}
}

func (h *Handler) MountRoutes(r chi.Router, mw web.Middlewares) {
	r.Post("/users", h.Register)
	r.Post("/users/login", h.Login)

	r.Group(func(r chi.Router) {
		r.Use(mw.Auth)
		r.Get("/user", h.GetCurrentUser)
		r.Put("/user", h.UpdateCurrentUser)
		r.Post("/profiles/{username}/follow", h.Follow)
		r.Delete("/profiles/{username}/follow", h.Unfollow)
	})

	r.Group(func(r chi.Router) {
		r.Use(mw.OptionalAuth)
		r.Get("/profiles/{username}", h.GetProfile)
	})
}

// --- DTOs ---

type userResponse struct {
	User struct {
		Email    string  `json:"email"`
		Token    string  `json:"token"`
		Username string  `json:"username"`
		Bio      string  `json:"bio"`
		Image    *string `json:"image"`
	} `json:"user"`
}

type profileResponse struct {
	Profile struct {
		Username  string  `json:"username"`
		Bio       string  `json:"bio"`
		Image     *string `json:"image"`
		Following bool    `json:"following"`
	} `json:"profile"`
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

// --- Handlers ---

func (h *Handler) Register(w http.ResponseWriter, r *http.Request) {
	var req registrationRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		web.RespondWithError(w, r, errors.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	user, err := h.service.Register(r.Context(), RegisterCommand{
		Username: req.User.Username,
		Email:    req.User.Email,
		Password: req.User.Password,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithUser(w, r, http.StatusCreated, user)
}

func (h *Handler) Login(w http.ResponseWriter, r *http.Request) {
	var req loginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		web.RespondWithError(w, r, errors.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	user, err := h.service.Login(r.Context(), LoginCommand{
		Email:    req.User.Email,
		Password: req.User.Password,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithUser(w, r, http.StatusOK, user)
}

func (h *Handler) GetCurrentUser(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	user, err := h.service.GetUser(r.Context(), userID)
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithUser(w, r, http.StatusOK, user)
}

func (h *Handler) UpdateCurrentUser(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	var req updateUserRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		web.RespondWithError(w, r, errors.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	user, err := h.service.UpdateUser(r.Context(), UpdateUserCommand{
		ID:       userID,
		Username: req.User.Username,
		Email:    req.User.Email,
		Password: req.User.Password,
		Bio:      req.User.Bio,
		Image:    req.User.Image,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithUser(w, r, http.StatusOK, user)
}

func (h *Handler) GetProfile(w http.ResponseWriter, r *http.Request) {
	username := chi.URLParam(r, "username")
	var observerID *int64
	if id, ok := web.GetUserIDFromContext(r.Context()); ok {
		observerID = &id
	}

	profile, err := h.service.GetProfile(r.Context(), username, observerID)
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithProfile(w, http.StatusOK, profile)
}

func (h *Handler) Follow(w http.ResponseWriter, r *http.Request) {
	username := chi.URLParam(r, "username")
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	profile, err := h.service.FollowUser(r.Context(), userID, username)
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithProfile(w, http.StatusOK, profile)
}

func (h *Handler) Unfollow(w http.ResponseWriter, r *http.Request) {
	username := chi.URLParam(r, "username")
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	profile, err := h.service.UnfollowUser(r.Context(), userID, username)
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithProfile(w, http.StatusOK, profile)
}

// --- Helpers ---

func (h *Handler) respondWithUser(w http.ResponseWriter, r *http.Request, code int, u *User) {
	token, err := h.tokenGenerator.Generate(u.ID)
	if err != nil {
		web.RespondWithError(w, r, errors.NewInternalError("failed to generate token"))
		return
	}

	var resp userResponse
	resp.User.Email = u.Email
	resp.User.Token = token
	resp.User.Username = u.Username
	resp.User.Bio = u.Bio
	resp.User.Image = u.Image

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}

func (h *Handler) respondWithProfile(w http.ResponseWriter, code int, profile *Profile) {
	var resp profileResponse
	resp.Profile.Username = profile.Username
	resp.Profile.Bio = profile.Bio
	resp.Profile.Image = profile.Image
	resp.Profile.Following = profile.Following

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
