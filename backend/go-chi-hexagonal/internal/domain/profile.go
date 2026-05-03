package domain

// Profile represents a user profile with following information.
type Profile struct {
	Username  string
	Bio       string
	Image     *string
	Following bool
}
