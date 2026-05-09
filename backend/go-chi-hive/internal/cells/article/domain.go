package article

import (
	"strings"
	"time"
)

// Article represents an article in the system.
type Article struct {
	ID             int64
	Slug           string
	Title          string
	Description    string
	Body           string
	TagList        []string
	CreatedAt      time.Time
	UpdatedAt      time.Time
	Favorited      bool
	FavoritesCount int
	Author         Author
}

// Author represents the article's author profile view within this cell.
type Author struct {
	Username  string
	Bio       string
	Image     *string
	Following bool
}

// Tag represents a keyword or category.
type Tag struct {
	Name string
}

// Update updates the article fields.
func (a *Article) Update(title, description, body *string, checkDuplicate func(title, slug string) error) error {
	if title != nil && *title != a.Title {
		newTitle := *title
		newSlug := Slugify(newTitle)
		if err := checkDuplicate(newTitle, newSlug); err != nil {
			return err
		}
		a.Title = newTitle
		a.Slug = newSlug
	}

	if description != nil {
		a.Description = *description
	}

	if body != nil {
		a.Body = *body
	}

	a.UpdatedAt = time.Now()
	return nil
}

// Slugify converts a title to a slug.
func Slugify(title string) string {
	return strings.ToLower(strings.ReplaceAll(title, " ", "-"))
}
