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

type articleRepository struct {
	db *sqlx.DB
}

func NewArticleRepository(db *sqlx.DB) port.ArticleRepository {
	return &articleRepository{
		db: db,
	}
}

type articleSchema struct {
	ID          int64     `db:"id"`
	Slug        string    `db:"slug"`
	Title       string    `db:"title"`
	Description string    `db:"description"`
	Body        string    `db:"body"`
	AuthorID    int64     `db:"fk_author"`
	CreatedAt   time.Time `db:"created_at"`
	UpdatedAt   time.Time `db:"updated_at"`
	// Joined fields
	AuthorUsername string         `db:"username"`
	AuthorBio      string         `db:"bio"`
	AuthorImage    sql.NullString `db:"image"`
	Following      bool           `db:"following"`
	Favorited      bool           `db:"favorited"`
	FavoritesCount int            `db:"favorites_count"`
}

func (s *articleSchema) toDomain(tags []string) *domain.Article {
	var image *string
	if s.AuthorImage.Valid {
		image = &s.AuthorImage.String
	}

	return &domain.Article{
		ID:             s.ID,
		Slug:           s.Slug,
		Title:          s.Title,
		Description:    s.Description,
		Body:           s.Body,
		TagList:        tags,
		CreatedAt:      s.CreatedAt,
		UpdatedAt:      s.UpdatedAt,
		Favorited:      s.Favorited,
		FavoritesCount: s.FavoritesCount,
		Author: domain.Profile{
			Username:  s.AuthorUsername,
			Bio:       s.AuthorBio,
			Image:     image,
			Following: s.Following,
		},
	}
}

func (r *articleRepository) Create(ctx context.Context, article *domain.Article, authorID int64) error {
	tx, err := r.db.BeginTxx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	query := `
		INSERT INTO article (slug, title, description, body, fk_author, created_at, updated_at)
		VALUES (:slug, :title, :description, :body, :fk_author, :created_at, :updated_at)
	`
	arg := map[string]any{
		"slug":        article.Slug,
		"title":       article.Title,
		"description": article.Description,
		"body":        article.Body,
		"fk_author":   authorID,
		"created_at":  article.CreatedAt,
		"updated_at":  article.UpdatedAt,
	}

	result, err := tx.NamedExecContext(ctx, query, arg)
	if err != nil {
		return err
	}

	id, err := result.LastInsertId()
	if err != nil {
		return err
	}
	article.ID = id

	if len(article.TagList) > 0 {
		err = r.createTags(ctx, tx, article)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

type tagRow struct {
	Tag string `db:"tag"`
}

type tagArticleRelationRow struct {
	ArticleId int64 `db:"article_id"`
	TagId     int64 `db:"tag_id"`
}

func (r *articleRepository) createTags(ctx context.Context, tx *sqlx.Tx, article *domain.Article) error {
	// 1. Insert all tags regardless of existence
	tagRows := make([]tagRow, len(article.TagList))
	for i, t := range article.TagList {
		tagRows[i] = tagRow{Tag: t}
	}

	_, err := tx.NamedExecContext(ctx, `
		INSERT INTO tag (tag) 
		VALUES (:tag) 
		ON CONFLICT (tag) DO NOTHING
	`, tagRows)
	if err != nil {
		return err
	}

	// 2. Get all tag IDs in one query
	query, args, err := sqlx.In("SELECT id FROM tag WHERE tag IN (?)", article.TagList)
	if err != nil {
		return err
	}
	query = tx.Rebind(query)

	var tagIDs []int64
	err = tx.SelectContext(ctx, &tagIDs, query, args...)
	if err != nil {
		return err
	}

	// 3. Bulk insert of the associations
	relRows := make([]tagArticleRelationRow, len(tagIDs))
	for i, id := range tagIDs {
		relRows[i] = tagArticleRelationRow{ArticleId: article.ID, TagId: id}
	}

	_, err = tx.NamedExecContext(ctx, `
			INSERT INTO tag_is_article_to_tag (article_id, tag_id)
			VALUES (:article_id, :tag_id)
        ON CONFLICT (article_id, tag_id) DO NOTHING
        `, relRows)
	if err != nil {
		return err
	}

	return nil
}

func (r *articleRepository) GetBySlug(ctx context.Context, slug string, observerID *int64) (*domain.Article, error) {
	return r.findOneBy(ctx, "slug", slug, observerID)
}

func (r *articleRepository) GetByTitle(ctx context.Context, title string, observerID *int64) (*domain.Article, error) {
	return r.findOneBy(ctx, "title", title, observerID)
}

func (r *articleRepository) GetArticles(ctx context.Context, params port.GetArticlesQuery) ([]*domain.Article, int, error) {
	queryBase := `
		FROM article a
		JOIN app_user u ON a.fk_author = u.id
	`

	if params.ObserverID != nil {
		queryBase += `
		LEFT JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = :observer_id
		LEFT JOIN favorite_is_article_to_user fav_obs ON a.id = fav_obs.article_id AND fav_obs.user_id = :observer_id
		`
	}

	if params.Tag != nil {
		queryBase += `
		JOIN tag_is_article_to_tag tat ON a.id = tat.article_id
		JOIN tag t ON tat.tag_id = t.id AND t.tag = :tag
		`
	}

	if params.Favorited != nil {
		queryBase += `
		JOIN favorite_is_article_to_user fav ON a.id = fav.article_id
		JOIN app_user u_fav ON fav.user_id = u_fav.id AND u_fav.username = :favorited
		`
	}

	whereClause := " WHERE 1=1"
	if params.Author != nil {
		whereClause += " AND u.username = :author"
	}

	args := map[string]any{
		"limit":  params.Limit,
		"offset": params.Offset,
	}

	if params.ObserverID != nil {
		args["observer_id"] = *params.ObserverID
	}
	if params.Tag != nil {
		args["tag"] = *params.Tag
	}
	if params.Favorited != nil {
		args["favorited"] = *params.Favorited
	}
	if params.Author != nil {
		args["author"] = *params.Author
	}

	// Count Query
	countQuery := "SELECT COUNT(DISTINCT a.id) " + queryBase + whereClause

	var count int
	// Need to use NamedQuery for named parameters with basic Query/QueryRow in sqlx
	countStmt, err := r.db.PrepareNamedContext(ctx, countQuery)
	if err != nil {
		return nil, 0, err
	}
	defer countStmt.Close()
	err = countStmt.GetContext(ctx, &count, args)
	if err != nil {
		return nil, 0, err
	}

	if count == 0 {
		return []*domain.Article{}, 0, nil
	}

	// Select Query
	selectQuery := `
		SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
		       u.username, u.bio, u.image,
	`
	if params.ObserverID != nil {
		selectQuery += `
		       CASE WHEN f.following_user_id IS NOT NULL THEN 1 ELSE 0 END as following,
		       CASE WHEN fav_obs.user_id IS NOT NULL THEN 1 ELSE 0 END as favorited,
		`
	} else {
		selectQuery += `
		       0 as following,
		       0 as favorited,
		`
	}
	selectQuery += `
		       (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count
	`
	selectQuery += queryBase + whereClause + `
		ORDER BY a.created_at DESC
		LIMIT :limit OFFSET :offset
	`

	var schemas []articleSchema
	selectStmt, err := r.db.PrepareNamedContext(ctx, selectQuery)
	if err != nil {
		return nil, 0, err
	}
	defer selectStmt.Close()
	err = selectStmt.SelectContext(ctx, &schemas, args)
	if err != nil {
		return nil, 0, err
	}

	articleIDs := make([]int64, len(schemas))
	for i, s := range schemas {
		articleIDs[i] = s.ID
	}

	tagsByArticle, err := r.getTagsForArticles(ctx, articleIDs)
	if err != nil {
		return nil, 0, err
	}

	articles := make([]*domain.Article, len(schemas))
	for i, s := range schemas {
		articles[i] = s.toDomain(tagsByArticle[s.ID])
	}

	return articles, count, nil
}

func (r *articleRepository) GetFeed(ctx context.Context, userID int64, limit, offset int) ([]*domain.Article, int, error) {
	query := `
		SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
		       u.username, u.bio, u.image,
		       1 as following,
		       CASE WHEN fav.user_id IS NOT NULL THEN 1 ELSE 0 END as favorited,
		       (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count
		FROM article a
		JOIN app_user u ON a.fk_author = u.id
		JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $1
		LEFT JOIN favorite_is_article_to_user fav ON a.id = fav.article_id AND fav.user_id = $1
		ORDER BY a.created_at DESC
		LIMIT $2 OFFSET $3
	`

	var schemas []articleSchema
	err := r.db.SelectContext(ctx, &schemas, query, userID, limit, offset)
	if err != nil {
		return nil, 0, err
	}

	var count int
	countQuery := `
		SELECT COUNT(*)
		FROM article a
		JOIN follow_is_user_to_user f ON a.fk_author = f.followed_user_id AND f.following_user_id = $1
	`
	err = r.db.GetContext(ctx, &count, countQuery, userID)
	if err != nil {
		return nil, 0, err
	}

	if len(schemas) == 0 {
		return []*domain.Article{}, count, nil
	}

	articleIDs := make([]int64, len(schemas))
	for i, s := range schemas {
		articleIDs[i] = s.ID
	}

	tagsByArticle, err := r.getTagsForArticles(ctx, articleIDs)
	if err != nil {
		return nil, 0, err
	}

	articles := make([]*domain.Article, len(schemas))
	for i, s := range schemas {
		articles[i] = s.toDomain(tagsByArticle[s.ID])
	}

	return articles, count, nil
}

func (r *articleRepository) Update(ctx context.Context, article *domain.Article) error {
	query := `
		UPDATE article 
		SET slug = :slug, title = :title, description = :description, body = :body, updated_at = :updated_at
		WHERE id = :id
	`
	arg := map[string]any{
		"id":          article.ID,
		"slug":        article.Slug,
		"title":       article.Title,
		"description": article.Description,
		"body":        article.Body,
		"updated_at":  article.UpdatedAt,
	}

	_, err := r.db.NamedExecContext(ctx, query, arg)
	return err
}

func (r *articleRepository) Delete(ctx context.Context, id int64) error {
	tx, err := r.db.BeginTxx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	// 1. Delete tag associations
	_, err = tx.ExecContext(ctx, "DELETE FROM tag_is_article_to_tag WHERE article_id = $1", id)
	if err != nil {
		return err
	}

	// 2. Delete favorites
	_, err = tx.ExecContext(ctx, "DELETE FROM favorite_is_article_to_user WHERE article_id = $1", id)
	if err != nil {
		return err
	}

	// 3. Delete comments
	_, err = tx.ExecContext(ctx, "DELETE FROM comment WHERE fk_article = $1", id)
	if err != nil {
		return err
	}

	// 4. Delete article
	_, err = tx.ExecContext(ctx, "DELETE FROM article WHERE id = $1", id)
	if err != nil {
		return err
	}

	return tx.Commit()
}

func (r *articleRepository) Favorite(ctx context.Context, articleID, userID int64) error {
	_, err := r.db.ExecContext(ctx, `
		INSERT INTO favorite_is_article_to_user (article_id, user_id)
		VALUES ($1, $2)
		ON CONFLICT (article_id, user_id) DO NOTHING
	`, articleID, userID)
	return err
}

func (r *articleRepository) Unfavorite(ctx context.Context, articleID, userID int64) error {
	_, err := r.db.ExecContext(ctx, `
		DELETE FROM favorite_is_article_to_user 
		WHERE article_id = $1 AND user_id = $2
	`, articleID, userID)
	return err
}

type articleTagRow struct {
	ArticleID int64  `db:"article_id"`
	Tag       string `db:"tag"`
}

func (r *articleRepository) getTagsForArticles(ctx context.Context, articleIDs []int64) (map[int64][]string, error) {
	if len(articleIDs) == 0 {
		return make(map[int64][]string), nil
	}

	query, args, err := sqlx.In(`
		SELECT tat.article_id, t.tag 
		FROM tag t
		JOIN tag_is_article_to_tag tat ON t.id = tat.tag_id
		WHERE tat.article_id IN (?)
	`, articleIDs)
	if err != nil {
		return nil, err
	}
	query = r.db.Rebind(query)

	var tagRows []articleTagRow
	if err := r.db.SelectContext(ctx, &tagRows, query, args...); err != nil {
		return nil, err
	}

	tagsByArticle := make(map[int64][]string)
	for _, tr := range tagRows {
		tagsByArticle[tr.ArticleID] = append(tagsByArticle[tr.ArticleID], tr.Tag)
	}
	return tagsByArticle, nil
}

func (r *articleRepository) findOneBy(ctx context.Context, column string, value any, observerID *int64) (*domain.Article, error) {
	var schema articleSchema
	var query string
	var args []any

	if observerID != nil {
		query = `
			SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
			       u.username, u.bio, u.image,
			       CASE WHEN f.following_user_id IS NOT NULL THEN 1 ELSE 0 END as following,
			       CASE WHEN fav.user_id IS NOT NULL THEN 1 ELSE 0 END as favorited,
			       (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count
			FROM article a
			JOIN app_user u ON a.fk_author = u.id
			LEFT JOIN follow_is_user_to_user f ON u.id = f.followed_user_id AND f.following_user_id = $2
			LEFT JOIN favorite_is_article_to_user fav ON a.id = fav.article_id AND fav.user_id = $2
			WHERE a.` + column + ` = $1
		`
		args = []any{value, *observerID}
	} else {
		query = `
			SELECT a.id, a.slug, a.title, a.description, a.body, a.fk_author, a.created_at, a.updated_at,
			       u.username, u.bio, u.image,
			       0 as following,
			       0 as favorited,
			       (SELECT COUNT(*) FROM favorite_is_article_to_user WHERE article_id = a.id) as favorites_count
			FROM article a
			JOIN app_user u ON a.fk_author = u.id
			WHERE a.` + column + ` = $1
		`
		args = []any{value}
	}

	err := r.db.GetContext(ctx, &schema, query, args...)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}

	tagsMap, err := r.getTagsForArticles(ctx, []int64{schema.ID})
	if err != nil {
		return nil, err
	}

	return schema.toDomain(tagsMap[schema.ID]), nil
}
