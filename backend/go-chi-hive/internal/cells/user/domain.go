package user

type User struct {
	ID       int64
	Username string
	Email    string
	Password string // Hashed password
	Bio      string
	Image    *string
}

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

type Profile struct {
	Username  string
	Bio       string
	Image     *string
	Following bool
}
