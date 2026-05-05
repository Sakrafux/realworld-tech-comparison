package testmocks

import (
	"context"

	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/application/port"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hexagonal/internal/domain"
	"github.com/stretchr/testify/mock"
)

// --- Inbound Ports (Services) ---

type MockTagService struct {
	mock.Mock
}

func (m *MockTagService) GetTags(ctx context.Context) ([]domain.Tag, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]domain.Tag), args.Error(1)
}

type MockUserService struct {
	mock.Mock
}

func (m *MockUserService) Register(ctx context.Context, cmd port.RegisterCommand) (*domain.User, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserService) Login(ctx context.Context, cmd port.LoginCommand) (*domain.User, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserService) GetUser(ctx context.Context, query port.GetUserQuery) (*domain.User, error) {
	args := m.Called(ctx, query)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserService) UpdateUser(ctx context.Context, cmd port.UpdateUserCommand) (*domain.User, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

// --- Outbound Ports (Infrastructure) ---

type MockTagRepository struct {
	mock.Mock
}

func (m *MockTagRepository) FindAll(ctx context.Context) ([]domain.Tag, error) {
	args := m.Called(ctx)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]domain.Tag), args.Error(1)
}

type MockUserRepository struct {
	mock.Mock
}

func (m *MockUserRepository) Create(ctx context.Context, user *domain.User) error {
	args := m.Called(ctx, user)
	return args.Error(0)
}

func (m *MockUserRepository) FindByEmail(ctx context.Context, email string) (*domain.User, error) {
	args := m.Called(ctx, email)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserRepository) FindByUsername(ctx context.Context, username string) (*domain.User, error) {
	args := m.Called(ctx, username)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserRepository) FindByID(ctx context.Context, id int64) (*domain.User, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.User), args.Error(1)
}

func (m *MockUserRepository) Update(ctx context.Context, user *domain.User) error {
	args := m.Called(ctx, user)
	return args.Error(0)
}

type MockPasswordHasher struct {
	mock.Mock
}

func (m *MockPasswordHasher) Hash(password string) (string, error) {
	args := m.Called(password)
	return args.String(0), args.Error(1)
}

func (m *MockPasswordHasher) Compare(hashedPassword, password string) error {
	args := m.Called(hashedPassword, password)
	return args.Error(0)
}

type MockTokenGenerator struct {
	mock.Mock
}

func (m *MockTokenGenerator) Generate(user *domain.User) (string, error) {
	args := m.Called(user)
	return args.String(0), args.Error(1)
}

func (m *MockTokenGenerator) Parse(token string) (int64, error) {
	args := m.Called(token)
	return args.Get(0).(int64), args.Error(1)
}

type MockProfileService struct {
	mock.Mock
}

func (m *MockProfileService) GetProfile(ctx context.Context, query port.GetProfileQuery) (*domain.Profile, error) {
	args := m.Called(ctx, query)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Profile), args.Error(1)
}

func (m *MockProfileService) FollowUser(ctx context.Context, cmd port.FollowUserCommand) (*domain.Profile, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Profile), args.Error(1)
}

func (m *MockProfileService) UnfollowUser(ctx context.Context, cmd port.UnfollowUserCommand) (*domain.Profile, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Profile), args.Error(1)
}

type MockProfileRepository struct {
	mock.Mock
}

func (m *MockProfileRepository) GetProfileByUsername(ctx context.Context, username string, observerID *int64) (*domain.Profile, error) {
	args := m.Called(ctx, username, observerID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Profile), args.Error(1)
}

func (m *MockProfileRepository) Follow(ctx context.Context, followerID, followedID int64) error {
	args := m.Called(ctx, followerID, followedID)
	return args.Error(0)
}

func (m *MockProfileRepository) Unfollow(ctx context.Context, followerID, followedID int64) error {
	args := m.Called(ctx, followerID, followedID)
	return args.Error(0)
}

type MockArticleService struct {
	mock.Mock
}

func (m *MockArticleService) CreateArticle(ctx context.Context, cmd port.CreateArticleCommand) (*domain.Article, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Article), args.Error(1)
}

func (m *MockArticleService) GetArticle(ctx context.Context, query port.GetArticleQuery) (*domain.Article, error) {
	args := m.Called(ctx, query)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Article), args.Error(1)
}

func (m *MockArticleService) UpdateArticle(ctx context.Context, cmd port.UpdateArticleCommand) (*domain.Article, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Article), args.Error(1)
}

func (m *MockArticleService) DeleteArticle(ctx context.Context, cmd port.DeleteArticleCommand) error {
	args := m.Called(ctx, cmd)
	return args.Error(0)
}

func (m *MockArticleService) FavoriteArticle(ctx context.Context, cmd port.FavoriteArticleCommand) (*domain.Article, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Article), args.Error(1)
}

func (m *MockArticleService) UnfavoriteArticle(ctx context.Context, cmd port.UnfavoriteArticleCommand) (*domain.Article, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Article), args.Error(1)
}

type MockArticleRepository struct {
	mock.Mock
}

func (m *MockArticleRepository) Create(ctx context.Context, article *domain.Article, authorID int64) error {
	args := m.Called(ctx, article, authorID)
	return args.Error(0)
}

func (m *MockArticleRepository) Update(ctx context.Context, article *domain.Article) error {
	args := m.Called(ctx, article)
	return args.Error(0)
}

func (m *MockArticleRepository) Delete(ctx context.Context, id int64) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *MockArticleRepository) Favorite(ctx context.Context, articleID, userID int64) error {
	args := m.Called(ctx, articleID, userID)
	return args.Error(0)
}

func (m *MockArticleRepository) Unfavorite(ctx context.Context, articleID, userID int64) error {
	args := m.Called(ctx, articleID, userID)
	return args.Error(0)
}

func (m *MockArticleRepository) GetBySlug(ctx context.Context, slug string, observerID *int64) (*domain.Article, error) {
	args := m.Called(ctx, slug, observerID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Article), args.Error(1)
}

func (m *MockArticleRepository) GetByTitle(ctx context.Context, title string, observerID *int64) (*domain.Article, error) {
	args := m.Called(ctx, title, observerID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Article), args.Error(1)
}

// --- Comments ---

type MockCommentService struct {
	mock.Mock
}

func (m *MockCommentService) CreateComment(ctx context.Context, cmd port.CreateCommentCommand) (*domain.Comment, error) {
	args := m.Called(ctx, cmd)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*domain.Comment), args.Error(1)
}

func (m *MockCommentService) GetComments(ctx context.Context, query port.GetCommentsQuery) ([]domain.Comment, error) {
	args := m.Called(ctx, query)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]domain.Comment), args.Error(1)
}

func (m *MockCommentService) DeleteComment(ctx context.Context, cmd port.DeleteCommentCommand) error {
	args := m.Called(ctx, cmd)
	return args.Error(0)
}

type MockCommentRepository struct {
	mock.Mock
}

func (m *MockCommentRepository) Create(ctx context.Context, comment *domain.Comment, articleID, authorID int64) error {
	args := m.Called(ctx, comment, articleID, authorID)
	return args.Error(0)
}

func (m *MockCommentRepository) FindByArticleID(ctx context.Context, articleID int64, observerID *int64) ([]domain.Comment, error) {
	args := m.Called(ctx, articleID, observerID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]domain.Comment), args.Error(1)
}

func (m *MockCommentRepository) GetByID(ctx context.Context, id int64) (*domain.Comment, int64, int64, error) {
	args := m.Called(ctx, id)
	if args.Get(0) == nil {
		return nil, 0, 0, args.Error(3)
	}
	return args.Get(0).(*domain.Comment), args.Get(1).(int64), args.Get(2).(int64), args.Error(3)
}

func (m *MockCommentRepository) Delete(ctx context.Context, id int64) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}
