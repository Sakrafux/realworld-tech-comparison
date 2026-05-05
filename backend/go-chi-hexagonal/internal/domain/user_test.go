package domain

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestUser_Update(t *testing.T) {
	t.Run("update all fields", func(t *testing.T) {
		initialImage := "old-image.jpg"
		u := &User{
			Username: "olduser",
			Email:    "old@example.com",
			Bio:      "old bio",
			Image:    &initialImage,
			Password: "old-hash",
		}

		newUsername := "newuser"
		newEmail := "new@example.com"
		newBio := "new bio"
		newImage := "new-image.jpg"
		newHash := "new-hash"

		u.Update(&newUsername, &newEmail, &newBio, &newImage, &newHash)

		assert.Equal(t, newUsername, u.Username)
		assert.Equal(t, newEmail, u.Email)
		assert.Equal(t, newBio, u.Bio)
		assert.Equal(t, &newImage, u.Image)
		assert.Equal(t, newHash, u.Password)
	})

	t.Run("partial update", func(t *testing.T) {
		u := &User{
			Username: "olduser",
			Email:    "old@example.com",
		}

		newBio := "new bio"
		u.Update(nil, nil, &newBio, nil, nil)

		assert.Equal(t, "olduser", u.Username)
		assert.Equal(t, "old@example.com", u.Email)
		assert.Equal(t, newBio, u.Bio)
		assert.Nil(t, u.Image)
	})

	t.Run("update image to empty string", func(t *testing.T) {
		image := "some-image.jpg"
		u := &User{Image: &image}

		newImage := ""
		u.Update(nil, nil, nil, &newImage, nil)

		assert.Equal(t, &newImage, u.Image)
		assert.Equal(t, "", *u.Image)
	})
}
