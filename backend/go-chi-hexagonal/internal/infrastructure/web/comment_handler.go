package web

import (
	"encoding/json"
	"net/http"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-playground/validator/v10"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type CommentHandler struct {
	commentService port.CommentService
	validate       *validator.Validate
}

func NewCommentHandler(commentService port.CommentService) *CommentHandler {
	return &CommentHandler{
		commentService: commentService,
		validate:       validator.New(),
	}
}

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

func (h *CommentHandler) CreateComment(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, r, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	var req createCommentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, r, domain.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		RespondWithError(w, r, err)
		return
	}

	comment, err := h.commentService.CreateComment(r.Context(), port.CreateCommentCommand{
		Slug:     slug,
		AuthorID: userID,
		Body:     req.Comment.Body,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	h.respondWithComment(w, http.StatusOK, comment)
}

func (h *CommentHandler) respondWithComment(w http.ResponseWriter, code int, comment *domain.Comment) {
	var resp commentResponse
	resp.Comment.ID = comment.ID
	resp.Comment.CreatedAt = comment.CreatedAt.Format(time.RFC3339)
	resp.Comment.UpdatedAt = comment.UpdatedAt.Format(time.RFC3339)
	resp.Comment.Body = comment.Body
	resp.Comment.Author.Username = comment.Author.Username
	resp.Comment.Author.Bio = comment.Author.Bio
	resp.Comment.Author.Image = comment.Author.Image
	resp.Comment.Author.Following = comment.Author.Following

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
