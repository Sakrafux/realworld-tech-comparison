package web

import (
	"encoding/json"
	"net/http"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type TagHandler struct {
	tagService port.TagService
}

func NewTagHandler(tagService port.TagService) *TagHandler {
	return &TagHandler{
		tagService: tagService,
	}
}

type tagsResponse struct {
	Tags []string `json:"tags"`
}

// GetTags returns all tags as a JSON response.
func (h *TagHandler) GetTags(w http.ResponseWriter, r *http.Request) {
	tags, err := h.tagService.GetTags(r.Context())
	if err != nil {
		RespondWithError(w, r, domain.NewInternalError(err.Error()))
		return
	}

	tagNames := make([]string, len(tags))
	for i, tag := range tags {
		tagNames[i] = tag.Name
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(tagsResponse{Tags: tagNames})
}
