package comment

import (
	"time"
)

// Comment represents a comment on an article.
type Comment struct {
	ID        int64
	Body      string
	CreatedAt time.Time
	UpdatedAt time.Time
	Author    Author
}

// Author represents the comment's author profile view.
type Author struct {
	Username  string
	Bio       string
	Image     *string
	Following bool
}
