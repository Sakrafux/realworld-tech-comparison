package domain

import "time"

// Comment represents a comment on an article.
type Comment struct {
	ID        int64
	Body      string
	CreatedAt time.Time
	UpdatedAt time.Time
	Author    Profile
}
