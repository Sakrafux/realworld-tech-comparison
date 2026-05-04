package persistence

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type userRepository struct {
	db *sqlx.DB
}

func NewUserRepository(db *sqlx.DB) port.UserRepository {
	return &userRepository{
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

func (s *userSchema) toDomain() *domain.User {
	var image *string
	if s.Image.Valid {
		image = &s.Image.String
	}
	return &domain.User{
		ID:       s.ID,
		Username: s.Username,
		Email:    s.Email,
		Password: s.Password,
		Bio:      s.Bio,
		Image:    image,
	}
}

func (r *userRepository) Create(ctx context.Context, user *domain.User) error {
	query := `
		INSERT INTO app_user (username, email, password, bio, image)
		VALUES (:username, :email, :password, :bio, :image)
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

	result, err := r.db.NamedExecContext(ctx, query, arg)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}
	user.ID = id
	return nil
}

func (r *userRepository) FindByEmail(ctx context.Context, email string) (*domain.User, error) {
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

func (r *userRepository) FindByUsername(ctx context.Context, username string) (*domain.User, error) {
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

func (r *userRepository) FindByID(ctx context.Context, id int64) (*domain.User, error) {
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

func (r *userRepository) Update(ctx context.Context, user *domain.User) error {
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
