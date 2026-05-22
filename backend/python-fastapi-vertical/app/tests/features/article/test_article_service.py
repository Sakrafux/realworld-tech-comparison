import pytest
from datetime import datetime
from unittest.mock import patch, AsyncMock

from features.article.domain import Article, Author, slugify
from features.article.repository import ArticlesList
from features.article import service
from shared.errors.app_error import ErrorType, AppError


@pytest.fixture
def author():
    return Author(username="testuser", bio="Test bio", image=None, following=False)


@pytest.fixture
def sample_article(author):
    return Article(
        id=1,
        slug="test-article",
        title="Test Article",
        description="A test description",
        body="Test body content",
        tag_list=["tag1", "tag2"],
        created_at=datetime(2025, 1, 1, 12, 0, 0),
        updated_at=datetime(2025, 1, 1, 12, 0, 0),
        favorited=False,
        favorites_count=0,
        author=author,
    )


@pytest.fixture
def favorited_article(author):
    return Article(
        id=1,
        slug="test-article",
        title="Test Article",
        description="A test description",
        body="Test body content",
        tag_list=["tag1", "tag2"],
        created_at=datetime(2025, 1, 1, 12, 0, 0),
        updated_at=datetime(2025, 1, 1, 12, 0, 0),
        favorited=True,
        favorites_count=1,
        author=author,
    )


class TestSlugify:
    def test_slugify_lowercase(self):
        assert slugify("Hello World") == "hello-world"

    def test_slugify_multiple_spaces(self):
        assert slugify("Hello   World") == "hello-world"

    def test_slugify_leading_trailing_spaces(self):
        assert slugify("  Hello World  ") == "-hello-world-"


@pytest.mark.asyncio
async def test_create_article_success(sample_article):
    from features.user.domain import User

    user = User(id=1, username="testuser", email="test@example.com", password="hashed", bio="Test bio", image=None)

    with patch("features.article.service.find_by_slug", new=AsyncMock(return_value=None)), \
         patch("features.article.service.find_user_by_id", new=AsyncMock(return_value=user)), \
         patch("features.article.service.create", new=AsyncMock()) as mock_create:

        article = await service.create_article(
            author_id=1,
            title="Test Article",
            description="A test description",
            body="Test body content",
            tag_list=["tag1", "tag2"],
        )

        assert article.slug == "test-article"
        assert article.title == "Test Article"
        assert article.description == "A test description"
        assert article.body == "Test body content"
        assert article.tag_list == ["tag1", "tag2"]
        assert article.favorited is False
        assert article.favorites_count == 0
        assert article.author.username == "testuser"
        mock_create.assert_called_once()


@pytest.mark.asyncio
async def test_create_article_duplicate_slug():
    existing_article = Article(
        id=2, slug="test-article", title="Existing", description="d", body="b",
        tag_list=[], created_at=datetime.now(), updated_at=datetime.now(),
        favorited=False, favorites_count=0,
        author=Author(username="other", bio="", image=None, following=False),
    )

    with patch("features.article.service.find_by_slug", new=AsyncMock(return_value=existing_article)):
        with pytest.raises(AppError) as exc_info:
            await service.create_article(
                author_id=1, title="Test Article",
                description="desc", body="body",
            )

        assert exc_info.value.error_type == ErrorType.ALREADY_EXISTS
        assert "Article with this title already exists" in exc_info.value.message


@pytest.mark.asyncio
async def test_create_article_author_not_found():
    with patch("features.article.service.find_by_slug", new=AsyncMock(return_value=None)), \
         patch("features.article.service.find_user_by_id", new=AsyncMock(return_value=None)):

        with pytest.raises(AppError) as exc_info:
            await service.create_article(
                author_id=999, title="Test",
                description="desc", body="body",
            )

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_get_article_success(sample_article):
    with patch("features.article.service.find_by_slug", new=AsyncMock(return_value=sample_article)):
        result = await service.get_article("test-article")

        assert result.slug == "test-article"
        assert result.title == "Test Article"


@pytest.mark.asyncio
async def test_get_article_not_found():
    with patch("features.article.service.find_by_slug", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.get_article("nonexistent")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "Article not found" in exc_info.value.message


@pytest.mark.asyncio
async def test_update_article_success(sample_article):
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=(sample_article, 1))), \
         patch("features.article.service.find_by_slug", new=AsyncMock(return_value=None)), \
         patch("features.article.service.update", new=AsyncMock()) as mock_update:

        result = await service.update_article(slug="test-article", user_id=1, description="Updated desc")

        assert result.description == "Updated desc"
        mock_update.assert_called_once()


@pytest.mark.asyncio
async def test_update_article_with_title_changes_slug(sample_article):
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=(sample_article, 1))), \
         patch("features.article.service.find_by_slug", new=AsyncMock(return_value=None)), \
         patch("features.article.service.update", new=AsyncMock()):

        result = await service.update_article(slug="test-article", user_id=1, title="New Title")

        assert result.title == "New Title"
        assert result.slug == "new-title"


@pytest.mark.asyncio
async def test_update_article_duplicate_slug_on_title_change(sample_article):
    existing_article = Article(
        id=2, slug="new-title", title="New Title", description="d", body="b",
        tag_list=[], created_at=datetime.now(), updated_at=datetime.now(),
        favorited=False, favorites_count=0,
        author=Author(username="other", bio="", image=None, following=False),
    )

    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=(sample_article, 1))), \
         patch("features.article.service.find_by_slug", new=AsyncMock(return_value=existing_article)):

        with pytest.raises(AppError) as exc_info:
            await service.update_article(slug="test-article", user_id=1, title="New Title")

        assert exc_info.value.error_type == ErrorType.ALREADY_EXISTS


@pytest.mark.asyncio
async def test_update_article_not_found():
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.update_article(slug="nonexistent", user_id=1, title="New")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_update_article_forbidden(sample_article):
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=(sample_article, 2))):
        with pytest.raises(AppError) as exc_info:
            await service.update_article(slug="test-article", user_id=1, title="New")

        assert exc_info.value.error_type == ErrorType.FORBIDDEN


@pytest.mark.asyncio
async def test_update_article_skip_slug_check_when_title_same(sample_article):
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=(sample_article, 1))), \
         patch("features.article.service.find_by_slug", new=AsyncMock()) as mock_find_by_slug, \
         patch("features.article.service.update", new=AsyncMock()):

        await service.update_article(slug="test-article", user_id=1, description="Updated")

        mock_find_by_slug.assert_not_called()


@pytest.mark.asyncio
async def test_delete_article_success(sample_article):
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=(sample_article, 1))), \
         patch("features.article.service.delete", new=AsyncMock()) as mock_delete:

        await service.delete_article("test-article", user_id=1)

        mock_delete.assert_called_once_with(1)


@pytest.mark.asyncio
async def test_delete_article_not_found():
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.delete_article("nonexistent", user_id=1)

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_delete_article_forbidden(sample_article):
    with patch("features.article.service.find_by_slug_for_author", new=AsyncMock(return_value=(sample_article, 2))):
        with pytest.raises(AppError) as exc_info:
            await service.delete_article("test-article", user_id=1)

        assert exc_info.value.error_type == ErrorType.FORBIDDEN


@pytest.mark.asyncio
async def test_favorite_article_success(sample_article, favorited_article):
    with patch("features.article.service.find_by_slug", new=AsyncMock(side_effect=[sample_article, favorited_article])), \
         patch("features.article.service.favorite", new=AsyncMock()) as mock_favorite:

        result = await service.favorite_article("test-article", user_id=1)

        mock_favorite.assert_called_once_with(1, 1)
        assert result.favorited is True
        assert result.favorites_count == 1


@pytest.mark.asyncio
async def test_favorite_article_not_found():
    with patch("features.article.service.find_by_slug", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.favorite_article("nonexistent", user_id=1)

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_unfavorite_article_success(sample_article):
    unfavorited = Article(
        id=1, slug="test-article", title="Test Article",
        description="A test description", body="Test body content",
        tag_list=["tag1", "tag2"],
        created_at=datetime(2025, 1, 1, 12, 0, 0),
        updated_at=datetime(2025, 1, 1, 12, 0, 0),
        favorited=False, favorites_count=0,
        author=Author(username="testuser", bio="Test bio", image=None, following=False),
    )

    favorited = Article(
        id=1, slug="test-article", title="Test Article",
        description="A test description", body="Test body content",
        tag_list=["tag1", "tag2"],
        created_at=datetime(2025, 1, 1, 12, 0, 0),
        updated_at=datetime(2025, 1, 1, 12, 0, 0),
        favorited=True, favorites_count=1,
        author=Author(username="testuser", bio="Test bio", image=None, following=False),
    )

    with patch("features.article.service.find_by_slug", new=AsyncMock(side_effect=[favorited, unfavorited])), \
         patch("features.article.service.unfavorite", new=AsyncMock()) as mock_unfavorite:

        result = await service.unfavorite_article("test-article", user_id=1)

        mock_unfavorite.assert_called_once_with(1, 1)
        assert result.favorited is False
        assert result.favorites_count == 0


@pytest.mark.asyncio
async def test_unfavorite_article_not_found():
    with patch("features.article.service.find_by_slug", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.unfavorite_article("nonexistent", user_id=1)

        assert exc_info.value.error_type == ErrorType.NOT_FOUND


@pytest.mark.asyncio
async def test_get_tags():
    with patch("features.article.service.find_all_tags", new=AsyncMock(return_value=["tag1", "tag2", "tag3"])):
        result = await service.get_tags()

        assert result == ["tag1", "tag2", "tag3"]


@pytest.mark.asyncio
async def test_get_articles_delegates_to_find_all():
    article = Article(
        id=1, slug="test-article", title="Test Article",
        description="A test description", body="Test body content",
        tag_list=["tag1"], created_at=datetime(2025, 1, 1), updated_at=datetime(2025, 1, 1),
        favorited=False, favorites_count=0,
        author=Author(username="testuser", bio="", image=None, following=False),
    )
    articles_list = ArticlesList(articles=[article], count=1)

    with patch("features.article.service.find_all", new=AsyncMock(return_value=articles_list)) as mock_find_all:
        result = await service.get_articles(tag="tag1", limit=20, offset=0)

        assert result.articles == [article]
        assert result.count == 1
        mock_find_all.assert_called_once_with(tag="tag1", author=None, favorited=None, limit=20, offset=0, observer_id=None)


@pytest.mark.asyncio
async def test_get_articles_with_observer():
    article = Article(
        id=1, slug="test-article", title="Test Article",
        description="A test description", body="Test body content",
        tag_list=[], created_at=datetime(2025, 1, 1), updated_at=datetime(2025, 1, 1),
        favorited=True, favorites_count=1,
        author=Author(username="testuser", bio="", image=None, following=True),
    )
    articles_list = ArticlesList(articles=[article], count=1)

    with patch("features.article.service.find_all", new=AsyncMock(return_value=articles_list)) as mock_find_all:
        result = await service.get_articles(observer_id=1)

        assert result.articles[0].favorited is True
        assert result.articles[0].author.following is True
        mock_find_all.assert_called_once_with(tag=None, author=None, favorited=None, limit=20, offset=0, observer_id=1)


@pytest.mark.asyncio
async def test_get_articles_empty():
    articles_list = ArticlesList(articles=[], count=0)

    with patch("features.article.service.find_all", new=AsyncMock(return_value=articles_list)):
        result = await service.get_articles()

        assert result.articles == []
        assert result.count == 0


@pytest.mark.asyncio
async def test_get_feed_delegates_to_find_feed():
    article = Article(
        id=1, slug="test-article", title="Test Article",
        description="A test description", body="Test body content",
        tag_list=[], created_at=datetime(2025, 1, 1), updated_at=datetime(2025, 1, 1),
        favorited=False, favorites_count=0,
        author=Author(username="followeduser", bio="", image=None, following=True),
    )
    articles_list = ArticlesList(articles=[article], count=1)

    with patch("features.article.service.find_feed", new=AsyncMock(return_value=articles_list)) as mock_find_feed:
        result = await service.get_feed(user_id=1, limit=20, offset=0)

        assert result.articles == [article]
        assert result.count == 1
        mock_find_feed.assert_called_once_with(user_id=1, limit=20, offset=0)


@pytest.mark.asyncio
async def test_get_feed_empty():
    articles_list = ArticlesList(articles=[], count=0)

    with patch("features.article.service.find_feed", new=AsyncMock(return_value=articles_list)):
        result = await service.get_feed(user_id=1)

        assert result.articles == []
        assert result.count == 0