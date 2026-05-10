package comment

import (
	"context"
	"time"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/cells/article"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/errors"
)

type service struct {
	repo            Repository
	articleProvider ArticleProvider
	userProvider    UserProvider
}

func NewService(repo Repository, articleProvider ArticleProvider, userProvider UserProvider) Service {
	return &service{
		repo:            repo,
		articleProvider: articleProvider,
		userProvider:    userProvider,
	}
}

func (s *service) CreateComment(ctx context.Context, cmd CreateCommentCommand) (*Comment, error) {
	art, err := s.articleProvider.GetArticle(ctx, article.GetArticleQuery{Slug: cmd.Slug, ObserverID: &cmd.AuthorID})
	if err != nil {
		return nil, err
	}

	u, err := s.userProvider.GetUser(ctx, cmd.AuthorID)
	if err != nil {
		return nil, err
	}

	comment := &Comment{
		Body:      cmd.Body,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
		Author: Author{
			Username:  u.Username,
			Bio:       u.Bio,
			Image:     u.Image,
			Following: false,
		},
	}

	if err := s.repo.Create(ctx, comment, art.ID, u.ID); err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return comment, nil
}

func (s *service) GetComments(ctx context.Context, query GetCommentsQuery) ([]Comment, error) {
	art, err := s.articleProvider.GetArticle(ctx, article.GetArticleQuery{Slug: query.Slug, ObserverID: query.ObserverID})
	if err != nil {
		return nil, err
	}

	comments, err := s.repo.FindByArticleID(ctx, art.ID, query.ObserverID)
	if err != nil {
		return nil, errors.NewInternalError(err.Error())
	}

	return comments, nil
}

func (s *service) DeleteComment(ctx context.Context, cmd DeleteCommentCommand) error {
	art, err := s.articleProvider.GetArticle(ctx, article.GetArticleQuery{Slug: cmd.Slug, ObserverID: nil})
	if err != nil {
		return err
	}

	comment, articleID, authorID, err := s.repo.GetByID(ctx, cmd.CommentID)
	if err != nil {
		return errors.NewInternalError(err.Error())
	}
	if comment == nil {
		return errors.NewResourceNotFound("Comment", "id", cmd.CommentID)
	}

	if articleID != art.ID {
		return errors.NewResourceNotFound("Comment", "id", cmd.CommentID)
	}

	if authorID != cmd.UserID {
		return errors.NewUnauthorizedError("user is not the author of the comment")
	}

	if err := s.repo.Delete(ctx, cmd.CommentID); err != nil {
		return errors.NewInternalError(err.Error())
	}

	return nil
}
