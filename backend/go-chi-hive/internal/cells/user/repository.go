package user

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"github.com/jmoiron/sqlx"
)

type repository struct {
	db *sqlx.DB
}

func NewRepository(db *sqlx.DB) Repository {
	return &repository{
		db: db,
	}
}

type userSchema struct {
	ID        int64          `db:"id"`
	Username  string         `db:"username"`
	Email     string         `db:"email"`
	Password  string         `db:"password"`
	Bio       string         `db:"bio"`
	Image     sql.NullString `db:"image"`
	Version   int            `db:"version"`
	CreatedAt time.Time      `db:"created_at"`
	UpdatedAt time.Time      `db:"updated_at"`
}

func (s *userSchema) toDomain() *User {
	var image *string
	if s.Image.Valid {
		image = &s.Image.String
	}
	return &User{
		ID:       s.ID,
		Username: s.Username,
		Email:    s.Email,
		Password: s.Password,
		Bio:      s.Bio,
		Image:    image,
	}
}

type profileSchema struct {
	Username  string         `db:"username"`
	Bio       string         `db:"bio"`
	Image     sql.NullString `db:"image"`
	Following bool           `db:"following"`
}

func (s *profileSchema) toDomain() *Profile {
	var image *string
	if s.Image.Valid {
		image = &s.Image.String
	}
	return &Profile{
		Username:  s.Username,
		Bio:       s.Bio,
		Image:     image,
		Following: s.Following,
	}
}

func (r *repository) Create(ctx context.Context, user *User) error {
	query := `
		INSERT INTO app_user (username, email, password, bio, image)
		VALUES (:username, :email, :password, :bio, :image)
		RETURNING id
	`
	var image sql.NullString
	if user.Image != nil {
		image = sql.NullString{String: *user.Image, Valid: true}
	}

	arg := map[string]any{
		"username": user.Username,
		"email":    user.Email,
		"password": user.Password,
		"bio":      user.Bio,
		"image":    image,
	}

	var id int64
	stmt, err := r.db.PrepareNamedContext(ctx, query)
	if err != nil {
		return err
	}
	defer stmt.Close()

	err = stmt.GetContext(ctx, &id, arg)
	if err != nil {
		return err
	}

	user.ID = id
	return nil
}

func (r *repository) FindByEmail(ctx context.Context, email string) (*User, error) {
	var schema userSchema
	query := `SELECT id, username, email, password, bio, image, version, created_at, updated_at FROM app_user WHERE email = $1`
	err := r.db.GetContext(ctx, &schema, query, email)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	return schema.toDomain(), nil
}

func (r *repository) FindByUsername(ctx context.Context, username string) (*User, error) {
	var schema userSchema
	query := `SELECT id, username, email, password, bio, image, version, created_at, updated_at FROM app_user WHERE username = $1`
	err := r.db.GetContext(ctx, &schema, query, username)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	return schema.toDomain(), nil
}

func (r *repository) FindByID(ctx context.Context, id int64) (*User, error) {
	var schema userSchema
	query := `SELECT id, username, email, password, bio, image, version, created_at, updated_at FROM app_user WHERE id = $1`
	err := r.db.GetContext(ctx, &schema, query, id)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	return schema.toDomain(), nil
}

func (r *repository) Update(ctx context.Context, user *User) error {
	query := `
		UPDATE app_user 
		SET username = :username, email = :email, password = :password, bio = :bio, image = :image, updated_at = CURRENT_TIMESTAMP, version = version + 1
		WHERE id = :id
	`
	var image sql.NullString
	if user.Image != nil {
		image = sql.NullString{String: *user.Image, Valid: true}
	}

	arg := map[string]any{
		"id":       user.ID,
		"username": user.Username,
		"email":    user.Email,
		"password": user.Password,
		"bio":      user.Bio,
		"image":    image,
	}

	_, err := r.db.NamedExecContext(ctx, query, arg)
	return err
}

func (r *repository) GetProfileByUsername(ctx context.Context, username string, observerID *int64) (*Profile, error) {
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

func (r *repository) Follow(ctx context.Context, followerID, followedID int64) error {
	query := `
		INSERT INTO follow_is_user_to_user (following_user_id, followed_user_id)
		VALUES ($1, $2)
		ON CONFLICT (following_user_id, followed_user_id) DO NOTHING
	`
	_, err := r.db.ExecContext(ctx, query, followerID, followedID)
	return err
}

func (r *repository) Unfollow(ctx context.Context, followerID, followedID int64) error {
	query := `
		DELETE FROM follow_is_user_to_user
		WHERE following_user_id = $1 AND followed_user_id = $2
	`
	_, err := r.db.ExecContext(ctx, query, followerID, followedID)
	return err
}
