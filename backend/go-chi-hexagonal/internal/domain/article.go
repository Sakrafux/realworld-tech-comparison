package domain

import "time"

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
	Author         Profile
}
