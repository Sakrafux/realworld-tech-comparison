package persistence

import (
	"context"
	"database/sql"
	"errors"

	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type profileRepository struct {
	db *sqlx.DB
}

func NewProfileRepository(db *sqlx.DB) port.ProfileRepository {
	return &profileRepository{
		db: db,
	}
}

type profileSchema struct {
	Username  string         `db:"username"`
	Bio       string         `db:"bio"`
	Image     sql.NullString `db:"image"`
	Following bool           `db:"following"`
}

func (s *profileSchema) toDomain() *domain.Profile {
	var image *string
	if s.Image.Valid {
		image = &s.Image.String
	}
	return &domain.Profile{
		Username:  s.Username,
		Bio:       s.Bio,
		Image:     image,
		Following: s.Following,
	}
}

func (r *profileRepository) GetProfileByUsername(ctx context.Context, username string, observerID *int64) (*domain.Profile, error) {
	var schema profileSchema
	var query string
	var args []any

	if observerID != nil {
		query = `
			SELECT u.username, u.bio, u.image,
			CASE WHEN f.following_user_id IS NOT NULL THEN 1 ELSE 0 END as following
			FROM app_user u
			LEFT JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $2
			WHERE u.username = $1
		`
		args = []any{username, *observerID}
	} else {
		query = `
			SELECT username, bio, image, 0 as following
			FROM app_user
			WHERE username = $1
		`
		args = []any{username}
	}

	err := r.db.GetContext(ctx, &schema, query, args...)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	return schema.toDomain(), nil
}

func (r *profileRepository) Follow(ctx context.Context, followerID, followedID int64) error {
	query := `
		INSERT INTO follow_is_user_to_user (following_user_id, followed_user_id)
		VALUES ($1, $2)
		ON CONFLICT (following_user_id, followed_user_id) DO NOTHING
	`
	_, err := r.db.ExecContext(ctx, query, followerID, followedID)
	return err
}

func (r *profileRepository) Unfollow(ctx context.Context, followerID, followedID int64) error {
	query := `
		DELETE FROM follow_is_user_to_user
		WHERE following_user_id = $1 AND followed_user_id = $2
	`
	_, err := r.db.ExecContext(ctx, query, followerID, followedID)
	return err
}
