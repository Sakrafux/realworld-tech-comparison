package comment

import (
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-playground/validator/v10"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/errors"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/web"
)

type Handler struct {
	service  Service
	validate *validator.Validate
}

func NewHandler(service Service) *Handler {
	return &Handler{
		service:  service,
		validate: validator.New(),
	}
}

func (h *Handler) MountRoutes(r chi.Router, mw web.Middlewares) {
	r.Group(func(r chi.Router) {
		r.Use(mw.OptionalAuth)
		r.Get("/articles/{slug}/comments", h.GetComments)
	})

	r.Group(func(r chi.Router) {
		r.Use(mw.Auth)
		r.Post("/articles/{slug}/comments", h.CreateComment)
		r.Delete("/articles/{slug}/comments/{id}", h.DeleteComment)
	})
}

// --- DTOs ---

type createCommentRequest struct {
	Comment struct {
		Body string `json:"body" validate:"required"`
	} `json:"comment" validate:"required"`
}

type commentResponse struct {
	Comment struct {
		ID        int64       `json:"id"`
		CreatedAt string      `json:"createdAt"`
		UpdatedAt string      `json:"updatedAt"`
		Body      string      `json:"body"`
		Author    profileData `json:"author"`
	} `json:"comment"`
}

type profileData struct {
	Username  string  `json:"username"`
	Bio       string  `json:"bio"`
	Image     *string `json:"image"`
	Following bool    `json:"following"`
}

// --- Handlers ---

func (h *Handler) CreateComment(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	var req createCommentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		web.RespondWithError(w, r, errors.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	c, err := h.service.CreateComment(r.Context(), CreateCommentCommand{
		Slug:     slug,
		AuthorID: userID,
		Body:     req.Comment.Body,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithComment(w, http.StatusOK, c)
}

func (h *Handler) GetComments(w http.ResponseWriter, r *http.Request) {
	slug := chi.URLParam(r, "slug")
	var observerID *int64
	if id, ok := web.GetUserIDFromContext(r.Context()); ok {
		observerID = &id
	}

	comments, err := h.service.GetComments(r.Context(), GetCommentsQuery{
		Slug:       slug,
		ObserverID: observerID,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithComments(w, http.StatusOK, comments)
}

func (h *Handler) DeleteComment(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")
	idStr := chi.URLParam(r, "id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		web.RespondWithError(w, r, errors.NewUnprocessableEntityError("invalid comment id"))
		return
	}

	err = h.service.DeleteComment(r.Context(), DeleteCommentCommand{
		Slug:      slug,
		CommentID: id,
		UserID:    userID,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusOK)
}

// --- Helpers ---

func (h *Handler) respondWithComment(w http.ResponseWriter, code int, c *Comment) {
	var resp commentResponse
	resp.Comment.ID = c.ID
	resp.Comment.CreatedAt = c.CreatedAt.Format(time.RFC3339)
	resp.Comment.UpdatedAt = c.UpdatedAt.Format(time.RFC3339)
	resp.Comment.Body = c.Body
	resp.Comment.Author.Username = c.Author.Username
	resp.Comment.Author.Bio = c.Author.Bio
	resp.Comment.Author.Image = c.Author.Image
	resp.Comment.Author.Following = c.Author.Following

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}

func (h *Handler) respondWithComments(w http.ResponseWriter, code int, comments []Comment) {
	type multiCommentResponse struct {
		Comments []struct {
			ID        int64       `json:"id"`
			CreatedAt string      `json:"createdAt"`
			UpdatedAt string      `json:"updatedAt"`
			Body      string      `json:"body"`
			Author    profileData `json:"author"`
		} `json:"comments"`
	}

	resp := multiCommentResponse{
		Comments: make([]struct {
			ID        int64       `json:"id"`
			CreatedAt string      `json:"createdAt"`
			UpdatedAt string      `json:"updatedAt"`
			Body      string      `json:"body"`
			Author    profileData `json:"author"`
		}, len(comments)),
	}

	for i, c := range comments {
		resp.Comments[i].ID = c.ID
		resp.Comments[i].CreatedAt = c.CreatedAt.Format(time.RFC3339)
		resp.Comments[i].UpdatedAt = c.UpdatedAt.Format(time.RFC3339)
		resp.Comments[i].Body = c.Body
		resp.Comments[i].Author.Username = c.Author.Username
		resp.Comments[i].Author.Bio = c.Author.Bio
		resp.Comments[i].Author.Image = c.Author.Image
		resp.Comments[i].Author.Following = c.Author.Following
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
