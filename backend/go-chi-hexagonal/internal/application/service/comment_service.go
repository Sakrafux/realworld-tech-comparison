package service

import (
	"context"
	"time"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
)

type commentService struct {
	commentRepo port.CommentRepository
	articleRepo port.ArticleRepository
	userRepo    port.UserRepository
}

func NewCommentService(commentRepo port.CommentRepository, articleRepo port.ArticleRepository, userRepo port.UserRepository) port.CommentService {
	return &commentService{
		commentRepo: commentRepo,
		articleRepo: articleRepo,
		userRepo:    userRepo,
	}
}

func (s *commentService) CreateComment(ctx context.Context, cmd port.CreateCommentCommand) (*domain.Comment, error) {
	article, err := s.articleRepo.GetBySlug(ctx, cmd.Slug, &cmd.AuthorID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, domain.NewResourceNotFound("Article", "slug", cmd.Slug)
	}

	user, err := s.userRepo.FindByID(ctx, cmd.AuthorID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if user == nil {
		return nil, domain.NewResourceNotFound("User", "id", cmd.AuthorID)
	}

	comment := &domain.Comment{
		Body:      cmd.Body,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
		Author: domain.Profile{
			Username:  user.Username,
			Bio:       user.Bio,
			Image:     user.Image,
			Following: false,
		},
	}

	if err := s.commentRepo.Create(ctx, comment, article.ID, user.ID); err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return comment, nil
}

func (s *commentService) GetComments(ctx context.Context, query port.GetCommentsQuery) ([]domain.Comment, error) {
	article, err := s.articleRepo.GetBySlug(ctx, query.Slug, query.ObserverID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}
	if article == nil {
		return nil, domain.NewResourceNotFound("Article", "slug", query.Slug)
	}

	comments, err := s.commentRepo.FindByArticleID(ctx, article.ID, query.ObserverID)
	if err != nil {
		return nil, domain.NewInternalError(err.Error())
	}

	return comments, nil
}

func (s *commentService) DeleteComment(ctx context.Context, cmd port.DeleteCommentCommand) error {
	article, err := s.articleRepo.GetBySlug(ctx, cmd.Slug, nil)
	if err != nil {
		return domain.NewInternalError(err.Error())
	}
	if article == nil {
		return domain.NewResourceNotFound("Article", "slug", cmd.Slug)
	}

	comment, articleID, authorID, err := s.commentRepo.GetByID(ctx, cmd.CommentID)
	if err != nil {
		return domain.NewInternalError(err.Error())
	}
	if comment == nil {
		return domain.NewResourceNotFound("Comment", "id", cmd.CommentID)
	}

	if articleID != article.ID {
		return domain.NewResourceNotFound("Comment", "id", cmd.CommentID)
	}

	if authorID != cmd.UserID {
		return domain.NewUnauthorizedError("user is not the author of the comment")
	}

	if err := s.commentRepo.Delete(ctx, cmd.CommentID); err != nil {
		return domain.NewInternalError(err.Error())
	}

	return nil
}
