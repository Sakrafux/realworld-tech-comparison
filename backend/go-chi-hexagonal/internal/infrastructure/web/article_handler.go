package web

import (
	"encoding/json"
	"net/http"
	"reflect"
	"strings"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-playground/validator/v10"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type ArticleHandler struct {
	articleService port.ArticleService
	validate       *validator.Validate
}

func NewArticleHandler(articleService port.ArticleService) *ArticleHandler {
	v := validator.New()

	v.RegisterTagNameFunc(func(fld reflect.StructField) string {
		name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
		if name == "-" {
			return ""
		}
		return name
	})

	return &ArticleHandler{
		articleService: articleService,
		validate:       v,
	}
}

type createArticleRequest struct {
	Article struct {
		Title       string   `json:"title" validate:"required,max=100"`
		Description string   `json:"description" validate:"required,max=255"`
		Body        string   `json:"body" validate:"required"`
		TagList     []string `json:"tagList" validate:"dive,required,max=20"`
	} `json:"article" validate:"required"`
}

type articleResponse struct {
	Article struct {
		Slug           string      `json:"slug"`
		Title          string      `json:"title"`
		Description    string      `json:"description"`
		Body           string      `json:"body"`
		TagList        []string    `json:"tagList"`
		CreatedAt      string      `json:"createdAt"`
		UpdatedAt      string      `json:"updatedAt"`
		Favorited      bool        `json:"favorited"`
		FavoritesCount int         `json:"favoritesCount"`
		Author         profileData `json:"author"`
	} `json:"article"`
}

type profileData struct {
	Username  string  `json:"username"`
	Bio       string  `json:"bio"`
	Image     *string `json:"image"`
	Following bool    `json:"following"`
}

func (h *ArticleHandler) CreateArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, r, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	var req createArticleRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, r, domain.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		RespondWithError(w, r, err)
		return
	}

	article, err := h.articleService.CreateArticle(r.Context(), port.CreateArticleCommand{
		AuthorID:    userID,
		Title:       req.Article.Title,
		Description: req.Article.Description,
		Body:        req.Article.Body,
		TagList:     req.Article.TagList,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusCreated, article)
}

func (h *ArticleHandler) GetArticle(w http.ResponseWriter, r *http.Request) {
	slug := chi.URLParam(r, "slug")
	var observerID *int64
	if id, ok := GetUserIDFromContext(r.Context()); ok {
		observerID = &id
	}

	article, err := h.articleService.GetArticle(r.Context(), port.GetArticleQuery{
		Slug:       slug,
		ObserverID: observerID,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

func (h *ArticleHandler) respondWithArticle(w http.ResponseWriter, code int, article *domain.Article) {
	var resp articleResponse
	resp.Article.Slug = article.Slug
	resp.Article.Title = article.Title
	resp.Article.Description = article.Description
	resp.Article.Body = article.Body
	resp.Article.TagList = article.TagList
	resp.Article.CreatedAt = article.CreatedAt.Format(time.RFC3339)
	resp.Article.UpdatedAt = article.UpdatedAt.Format(time.RFC3339)
	resp.Article.Favorited = article.Favorited
	resp.Article.FavoritesCount = article.FavoritesCount
	resp.Article.Author.Username = article.Author.Username
	resp.Article.Author.Bio = article.Author.Bio
	resp.Article.Author.Image = article.Author.Image
	resp.Article.Author.Following = article.Author.Following

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
