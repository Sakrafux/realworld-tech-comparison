import pytest
from datetime import datetime
from unittest.mock import patch, AsyncMock

from features.article.domain import Article, Author as ArticleAuthor
from features.comment.domain import Comment, CommentAuthor
from features.comment import service
from shared.errors.app_error import ErrorType, AppError


@pytest.fixture
def comment_author():
    return CommentAuthor(username="testuser", bio="Test bio", image=None, following=False)


@pytest.fixture
def sample_comment(comment_author):
    return Comment(
        id=1,
        created_at=datetime(2025, 1, 1, 12, 0, 0),
        updated_at=datetime(2025, 1, 1, 12, 0, 0),
        body="Test comment body",
        author=comment_author,
    )


@pytest.fixture
def sample_article():
    return Article(
        id=1,
        slug="test-article",
        title="Test Article",
        description="A test description",
        body="Test body content",
        tag_list=[],
        created_at=datetime(2025, 1, 1, 12, 0, 0),
        updated_at=datetime(2025, 1, 1, 12, 0, 0),
        favorited=False,
        favorites_count=0,
        author=ArticleAuthor(username="author", bio="", image=None, following=False),
    )


@pytest.mark.asyncio
async def test_create_comment_success(sample_article, sample_comment):
    from features.user.domain import User

    user = User(id=1, username="testuser", email="test@example.com", password="hashed", bio="Test bio", image=None)

    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=sample_article)), \
         patch("features.comment.service.find_user_by_id", new=AsyncMock(return_value=user)), \
         patch("features.comment.service.create", new=AsyncMock()) as mock_create:

        comment = await service.create_comment(slug="test-article", author_id=1, body="Test comment body")

        assert comment.body == "Test comment body"
        assert comment.author.username == "testuser"
        mock_create.assert_called_once()


@pytest.mark.asyncio
async def test_create_comment_article_not_found():
    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.create_comment(slug="nonexistent", author_id=1, body="Test")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "Article not found" in exc_info.value.message


@pytest.mark.asyncio
async def test_create_comment_user_not_found(sample_article):
    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=sample_article)), \
         patch("features.comment.service.find_user_by_id", new=AsyncMock(return_value=None)):

        with pytest.raises(AppError) as exc_info:
            await service.create_comment(slug="test-article", author_id=999, body="Test")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_get_comments_success(sample_article, sample_comment):
    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=sample_article)), \
         patch("features.comment.service.find_by_article_id", new=AsyncMock(return_value=[sample_comment])):

        result = await service.get_comments(slug="test-article")

        assert len(result) == 1
        assert result[0].body == "Test comment body"


@pytest.mark.asyncio
async def test_get_comments_article_not_found():
    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.get_comments(slug="nonexistent")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_get_comments_with_observer(sample_article, comment_author):
    comment = Comment(
        id=1, created_at=datetime(2025, 1, 1), updated_at=datetime(2025, 1, 1),
        body="Test comment", author=CommentAuthor(username="other", bio="", image=None, following=True),
    )

    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=sample_article)), \
         patch("features.comment.service.find_by_article_id", new=AsyncMock(return_value=[comment])):

        result = await service.get_comments(slug="test-article", observer_id=1)
        assert result[0].author.following is True


@pytest.mark.asyncio
async def test_delete_comment_success(sample_article):
    comment = Comment(
        id=5, created_at=datetime(2025, 1, 1), updated_at=datetime(2025, 1, 1),
        body="Test", author=CommentAuthor(username="testuser", bio="", image=None),
    )

    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=sample_article)), \
         patch("features.comment.service.get_by_id", new=AsyncMock(return_value=(comment, sample_article.id, 1))), \
         patch("features.comment.service.delete", new=AsyncMock()) as mock_delete:

        await service.delete_comment(slug="test-article", comment_id=5, user_id=1)

        mock_delete.assert_called_once_with(5)


@pytest.mark.asyncio
async def test_delete_comment_article_not_found():
    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.delete_comment(slug="nonexistent", comment_id=1, user_id=1)

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_delete_comment_not_found(sample_article):
    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=sample_article)), \
         patch("features.comment.service.get_by_id", new=AsyncMock(return_value=None)):

        with pytest.raises(AppError) as exc_info:
            await service.delete_comment(slug="test-article", comment_id=999, user_id=1)

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_delete_comment_forbidden(sample_article):
    comment = Comment(
        id=5, created_at=datetime(2025, 1, 1), updated_at=datetime(2025, 1, 1),
        body="Test", author=CommentAuthor(username="otheruser", bio="", image=None),
    )

    with patch("features.comment.service.find_by_slug", new=AsyncMock(return_value=sample_article)), \
         patch("features.comment.service.get_by_id", new=AsyncMock(return_value=(comment, sample_article.id, 2))):

        with pytest.raises(AppError) as exc_info:
            await service.delete_comment(slug="test-article", comment_id=5, user_id=1)

        assert exc_info.value.error_type == ErrorType.FORBIDDEN