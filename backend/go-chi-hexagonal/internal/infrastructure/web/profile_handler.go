package web

import (
	"context"
	"encoding/json"
	"net/http"

	"github.com/go-chi/chi/v5"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type ProfileHandler struct {
	profileService port.ProfileService
}

// NewProfileHandler creates a new profile handler.
func NewProfileHandler(profileService port.ProfileService) *ProfileHandler {
	return &ProfileHandler{
		profileService: profileService,
	}
}

type profileResponse struct {
	Profile struct {
		Username  string  `json:"username"`
		Bio       string  `json:"bio"`
		Image     *string `json:"image"`
		Following bool    `json:"following"`
	} `json:"profile"`
}

func (h *ProfileHandler) GetProfile(w http.ResponseWriter, r *http.Request) {
	username := chi.URLParam(r, "username")
	var observerID *int64
	if id, ok := GetUserIDFromContext(r.Context()); ok {
		observerID = &id
	}

	profile, err := h.profileService.GetProfile(r.Context(), port.GetProfileQuery{
		Username:   username,
		ObserverID: observerID,
	})
	if err != nil {
		RespondWithError(w, err)
		return
	}

	h.respondWithProfile(w, http.StatusOK, profile)
}

func (h *ProfileHandler) Follow(w http.ResponseWriter, r *http.Request) {
	h.handleFollowAction(w, r, func(ctx context.Context, userID int64, username string) (*domain.Profile, error) {
		return h.profileService.FollowUser(ctx, port.FollowUserCommand{
			FollowerID: userID,
			Username:   username,
		})
	})
}

func (h *ProfileHandler) Unfollow(w http.ResponseWriter, r *http.Request) {
	h.handleFollowAction(w, r, func(ctx context.Context, userID int64, username string) (*domain.Profile, error) {
		return h.profileService.UnfollowUser(ctx, port.UnfollowUserCommand{
			FollowerID: userID,
			Username:   username,
		})
	})
}

func (h *ProfileHandler) handleFollowAction(
	w http.ResponseWriter,
	r *http.Request,
	action func(ctx context.Context, userID int64, username string) (*domain.Profile, error),
) {
	username := chi.URLParam(r, "username")
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	profile, err := action(r.Context(), userID, username)
	if err != nil {
		RespondWithError(w, err)
		return
	}

	h.respondWithProfile(w, http.StatusOK, profile)
}

func (h *ProfileHandler) respondWithProfile(w http.ResponseWriter, code int, profile *domain.Profile) {
	var resp profileResponse
	resp.Profile.Username = profile.Username
	resp.Profile.Bio = profile.Bio
	resp.Profile.Image = profile.Image
	resp.Profile.Following = profile.Following

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
