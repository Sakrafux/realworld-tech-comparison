package article

import (
	"encoding/json"
	"net/http"
	"reflect"
	"strconv"
	"strings"
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
	v := validator.New()

	v.RegisterTagNameFunc(func(fld reflect.StructField) string {
		name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
		if name == "-" {
			return ""
		}
		return name
	})

	return &Handler{
		service:  service,
		validate: v,
	}
}

func (h *Handler) MountRoutes(r chi.Router, mw web.Middlewares) {
	r.Get("/tags", h.GetTags)

	r.Group(func(r chi.Router) {
		r.Use(mw.OptionalAuth)
		r.Get("/articles", h.GetArticles)
		r.Get("/articles/{slug}", h.GetArticle)
	})

	r.Group(func(r chi.Router) {
		r.Use(mw.Auth)
		r.Get("/articles/feed", h.GetFeed)
		r.Post("/articles", h.CreateArticle)
		r.Put("/articles/{slug}", h.UpdateArticle)
		r.Delete("/articles/{slug}", h.DeleteArticle)
		r.Post("/articles/{slug}/favorite", h.FavoriteArticle)
		r.Delete("/articles/{slug}/favorite", h.UnfavoriteArticle)
	})
}

// --- DTOs ---

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

type tagsResponse struct {
	Tags []string `json:"tags"`
}

// --- Handlers ---

func (h *Handler) GetTags(w http.ResponseWriter, r *http.Request) {
	tags, err := h.service.GetTags(r.Context())
	if err != nil {
		web.RespondWithError(w, r, errors.NewInternalError(err.Error()))
		return
	}

	tagNames := make([]string, len(tags))
	for i, tag := range tags {
		tagNames[i] = tag.Name
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(tagsResponse{Tags: tagNames})
}

func (h *Handler) CreateArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	var req createArticleRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		web.RespondWithError(w, r, errors.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	article, err := h.service.CreateArticle(r.Context(), CreateArticleCommand{
		AuthorID:    userID,
		Title:       req.Article.Title,
		Description: req.Article.Description,
		Body:        req.Article.Body,
		TagList:     req.Article.TagList,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusCreated, article)
}

func (h *Handler) GetArticles(w http.ResponseWriter, r *http.Request) {
	var observerID *int64
	if id, ok := web.GetUserIDFromContext(r.Context()); ok {
		observerID = &id
	}

	limit, offset, err := h.parsePagination(r)
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	query := GetArticlesQuery{
		Limit:      limit,
		Offset:     offset,
		ObserverID: observerID,
	}

	if tag := r.URL.Query().Get("tag"); tag != "" {
		query.Tag = &tag
	}
	if author := r.URL.Query().Get("author"); author != "" {
		query.Author = &author
	}
	if favorited := r.URL.Query().Get("favorited"); favorited != "" {
		query.Favorited = &favorited
	}

	articles, count, err := h.service.GetArticles(r.Context(), query)
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithMultipleArticles(w, http.StatusOK, articles, count)
}

func (h *Handler) GetFeed(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	limit, offset, err := h.parsePagination(r)
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	articles, count, err := h.service.GetFeed(r.Context(), GetFeedQuery{
		UserID: userID,
		Limit:  limit,
		Offset: offset,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithMultipleArticles(w, http.StatusOK, articles, count)
}

func (h *Handler) GetArticle(w http.ResponseWriter, r *http.Request) {
	slug := chi.URLParam(r, "slug")
	var observerID *int64
	if id, ok := web.GetUserIDFromContext(r.Context()); ok {
		observerID = &id
	}

	article, err := h.service.GetArticle(r.Context(), GetArticleQuery{
		Slug:       slug,
		ObserverID: observerID,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

func (h *Handler) UpdateArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	var req updateArticleRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		web.RespondWithError(w, r, errors.NewUnprocessableEntityError("invalid request body"))
		return
	}

	if err := h.validate.Struct(req); err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	article, err := h.service.UpdateArticle(r.Context(), UpdateArticleCommand{
		Slug:        slug,
		UserID:      userID,
		Title:       req.Article.Title,
		Description: req.Article.Description,
		Body:        req.Article.Body,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

func (h *Handler) DeleteArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	err := h.service.DeleteArticle(r.Context(), DeleteArticleCommand{
		Slug:   slug,
		UserID: userID,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	w.WriteHeader(http.StatusOK)
}

func (h *Handler) FavoriteArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	article, err := h.service.FavoriteArticle(r.Context(), FavoriteArticleCommand{
		Slug:   slug,
		UserID: userID,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

func (h *Handler) UnfavoriteArticle(w http.ResponseWriter, r *http.Request) {
	userID, ok := web.GetUserIDFromContext(r.Context())
	if !ok {
		web.RespondWithError(w, r, errors.NewUnauthorizedError("user not found in context"))
		return
	}

	slug := chi.URLParam(r, "slug")

	article, err := h.service.UnfavoriteArticle(r.Context(), UnfavoriteArticleCommand{
		Slug:   slug,
		UserID: userID,
	})
	if err != nil {
		web.RespondWithError(w, r, err)
		return
	}

	h.respondWithArticle(w, http.StatusOK, article)
}

// --- Helpers ---

func (h *Handler) respondWithArticle(w http.ResponseWriter, code int, article *Article) {
	resp := articleResponse{
		Article: mapArticleToResponseData(article),
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(resp)
}

func mapArticleToResponseData(article *Article) articleResponseData {
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

func (h *Handler) respondWithMultipleArticles(w http.ResponseWriter, code int, articles []*Article, count int) {
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

func (h *Handler) parsePagination(r *http.Request) (int, int, error) {
	limit := 20
	if limitStr := r.URL.Query().Get("limit"); limitStr != "" {
		l, err := strconv.Atoi(limitStr)
		if err != nil {
			return 0, 0, errors.NewUnprocessableEntityError("limit must be a number")
		}
		limit = l
	}
	if limit < 1 {
		return 0, 0, errors.NewUnprocessableEntityError("limit must be at least 1")
	}

	offset := 0
	if offsetStr := r.URL.Query().Get("offset"); offsetStr != "" {
		o, err := strconv.Atoi(offsetStr)
		if err != nil {
			return 0, 0, errors.NewUnprocessableEntityError("offset must be a number")
		}
		offset = o
	}
	if offset < 0 {
		return 0, 0, errors.NewUnprocessableEntityError("offset must be at least 0")
	}
	return limit, offset, nil
}
