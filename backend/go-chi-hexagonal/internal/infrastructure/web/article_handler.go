package web

import (
	"encoding/json"
	"net/http"
	"reflect"
	"strconv"
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

type updateArticleRequest struct {
	Article struct {
		Title       *string `json:"title" validate:"omitempty,max=100"`
		Description *string `json:"description" validate:"omitempty,max=255"`
		Body        *string `json:"body" validate:"omitempty"`
	} `json:"article" validate:"required"`
}

type articleResponseData struct {
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
}

type articleResponse struct {
	Article articleResponseData `json:"article"`
}

type multipleArticlesResponse struct {
	Articles      []articleResponseData `json:"articles"`
	ArticlesCount int                   `json:"articlesCount"`
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

func (h *ArticleHandler) GetFeed(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, r, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	limit := 20
	if limitStr := r.URL.Query().Get("limit"); limitStr != "" {
		if l, err := strconv.Atoi(limitStr); err == nil {
			limit = l
		}
	}
	if limit < 1 {
		RespondWithError(w, r, domain.NewUnprocessableEntityError("limit must be at least 1"))
		return
	}

	offset := 0
	if offsetStr := r.URL.Query().Get("offset"); offsetStr != "" {
		if o, err := strconv.Atoi(offsetStr); err == nil {
			offset = o
		}
	}
	if offset < 0 {
		RespondWithError(w, r, domain.NewUnprocessableEntityError("offset must be at least 0"))
		return
	}

	articles, count, err := h.articleService.GetFeed(r.Context(), port.GetFeedQuery{
		UserID: userID,
		Limit:  limit,
		Offset: offset,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	h.respondWithMultipleArticles(w, http.StatusOK, articles, count)
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

func (h *ArticleHandler) UpdateArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, r, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	var req updateArticleRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		RespondWithError(w, r, domain.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		RespondWithError(w, r, err)
		return
	}

	article, err := h.articleService.UpdateArticle(r.Context(), port.UpdateArticleCommand{
		Slug:        slug,
		UserID:      userID,
		Title:       req.Article.Title,
		Description: req.Article.Description,
		Body:        req.Article.Body,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

func (h *ArticleHandler) DeleteArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, r, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	err := h.articleService.DeleteArticle(r.Context(), port.DeleteArticleCommand{
		Slug:   slug,
		UserID: userID,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusOK)
}

func (h *ArticleHandler) FavoriteArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, r, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	article, err := h.articleService.FavoriteArticle(r.Context(), port.FavoriteArticleCommand{
		Slug:   slug,
		UserID: userID,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

func (h *ArticleHandler) UnfavoriteArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		RespondWithError(w, r, domain.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	article, err := h.articleService.UnfavoriteArticle(r.Context(), port.UnfavoriteArticleCommand{
		Slug:   slug,
		UserID: userID,
	})
	if err != nil {
		RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

func (h *ArticleHandler) respondWithArticle(w http.ResponseWriter, code int, article *domain.Article) {
	resp := articleResponse{
		Article: mapArticleToResponseData(article),
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}

func mapArticleToResponseData(article *domain.Article) articleResponseData {
	if article.TagList == nil {
		article.TagList = []string{}
	}
	return articleResponseData{
		Slug:           article.Slug,
		Title:          article.Title,
		Description:    article.Description,
		Body:           article.Body,
		TagList:        article.TagList,
		CreatedAt:      article.CreatedAt.Format(time.RFC3339),
		UpdatedAt:      article.UpdatedAt.Format(time.RFC3339),
		Favorited:      article.Favorited,
		FavoritesCount: article.FavoritesCount,
		Author: profileData{
			Username:  article.Author.Username,
			Bio:       article.Author.Bio,
			Image:     article.Author.Image,
			Following: article.Author.Following,
		},
	}
}

func (h *ArticleHandler) respondWithMultipleArticles(w http.ResponseWriter, code int, articles []*domain.Article, count int) {
	resp := multipleArticlesResponse{
		Articles:      make([]articleResponseData, len(articles)),
		ArticlesCount: count,
	}
	for i, a := range articles {
		resp.Articles[i] = mapArticleToResponseData(a)
	}
	if resp.Articles == nil {
		resp.Articles = make([]articleResponseData, 0)
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}
