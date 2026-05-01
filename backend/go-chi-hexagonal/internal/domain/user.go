package domain

import "time"

// User represents a user of the system.
type User struct {
	ID        int64
	Username  string
	Email     string
	Password  string // Hashed password
	Bio       string
	Image     *string
	CreatedAt time.Time
	UpdatedAt time.Time
}

// Update modifies the user fields if they are provided (not nil).
// Password should be passed already hashed.
func (u *User) Update(username, email, bio, image, hashedPassword *string) {
	if username != nil {
		u.Username = *username
	}
	if email != nil {
		u.Email = *email
	}
	if bio != nil {
		u.Bio = *bio
	}
	if image != nil {
		u.Image = image
	}
	if hashedPassword != nil {
		u.Password = *hashedPassword
	}
}
