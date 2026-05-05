package domain

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestArticle_Update(t *testing.T) {
	t.Run("update title and slug success", func(t *testing.T) {
		art := &Article{ID: 1, Title: "Old Title", Slug: "old-title"}
		newTitle := "New Title"

		checkDuplicate := func(title, slug string) error {
			assert.Equal(t, "New Title", title)
			assert.Equal(t, "new-title", slug)
			return nil
		}

		err := art.Update(&newTitle, nil, nil, checkDuplicate)

		assert.NoError(t, err)
		assert.Equal(t, "New Title", art.Title)
		assert.Equal(t, "new-title", art.Slug)
	})

	t.Run("update title duplicate", func(t *testing.T) {
		art := &Article{ID: 1, Title: "Old Title", Slug: "old-title"}
		newTitle := "Duplicate"

		checkDuplicate := func(title, slug string) error {
			return NewAlreadyExistsError("duplicate")
		}

		err := art.Update(&newTitle, nil, nil, checkDuplicate)

		assert.Error(t, err)
		assert.Equal(t, TypeAlreadyExists, err.(AppError).Type)
		assert.Equal(t, "Old Title", art.Title) // Should not change on error
	})

	t.Run("update description and body", func(t *testing.T) {
		art := &Article{Title: "Title", Description: "Old Desc", Body: "Old Body"}
		newDesc := "New Desc"
		newBody := "New Body"

		err := art.Update(nil, &newDesc, &newBody, nil)

		assert.NoError(t, err)
		assert.Equal(t, "New Desc", art.Description)
		assert.Equal(t, "New Body", art.Body)
		assert.Equal(t, "Title", art.Title)
	})
}

func TestSlugify(t *testing.T) {
	assert.Equal(t, "hello-world", Slugify("Hello World"))
	assert.Equal(t, "test", Slugify("test"))
	assert.Equal(t, "multiple--spaces", Slugify("Multiple  Spaces"))
}
