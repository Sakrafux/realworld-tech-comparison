import pytest
from datetime import datetime
from unittest.mock import patch, AsyncMock

import jwt
from fastapi.testclient import TestClient

from features.article.domain import Article, Author
from main import app
from shared.config.env import settings
from shared.errors.app_error import ErrorType, AppError


@pytest.fixture
def client():
    with patch("main.get_pool", new_callable=AsyncMock):
        with TestClient(app) as c:
            yield c


@pytest.fixture
def auth_token():
    return jwt.encode({"id": 1}, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)


@pytest.fixture
def auth_headers(auth_token):
    return {"Authorization": f"Token {auth_token}"}


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


class TestCreateArticle:
    def test_create_article_success(self, client, sample_article, auth_headers):
        with patch("features.article.controller.create_article", new=AsyncMock(return_value=sample_article)):
            response = client.post("/api/articles", json={
                "article": {
                    "title": "Test Article",
                    "description": "A test description",
                    "body": "Test body content",
                    "tagList": ["tag1", "tag2"],
                }
            }, headers=auth_headers)

            assert response.status_code == 201
            data = response.json()
            assert data["article"]["slug"] == "test-article"
            assert data["article"]["title"] == "Test Article"
            assert data["article"]["description"] == "A test description"
            assert data["article"]["body"] == "Test body content"
            assert data["article"]["tagList"] == ["tag1", "tag2"]
            assert data["article"]["favorited"] is False
            assert data["article"]["favoritesCount"] == 0
            assert data["article"]["author"]["username"] == "testuser"
            assert data["article"]["author"]["bio"] == "Test bio"

    def test_create_article_unauthorized(self, client):
        response = client.post("/api/articles", json={
            "article": {
                "title": "Test Article",
                "description": "A test description",
                "body": "Test body content",
            }
        })

        assert response.status_code == 401

    def test_create_article_validation_error_empty_title(self, client, auth_headers):
        response = client.post("/api/articles", json={
            "article": {
                "title": "",
                "description": "A test description",
                "body": "Test body content",
            }
        }, headers=auth_headers)

        assert response.status_code == 422

    def test_create_article_validation_error_missing_body(self, client, auth_headers):
        response = client.post("/api/articles", json={
            "article": {
                "title": "Test Article",
                "description": "A test description",
            }
        }, headers=auth_headers)

        assert response.status_code == 422

    def test_create_article_validation_error_tag_too_long(self, client, auth_headers):
        response = client.post("/api/articles", json={
            "article": {
                "title": "Test Article",
                "description": "A test description",
                "body": "Test body content",
                "tagList": ["a" * 21],
            }
        }, headers=auth_headers)

        assert response.status_code == 422

    def test_create_article_duplicate_slug(self, client, auth_headers):
        with patch("features.article.controller.create_article", new=AsyncMock(
            side_effect=AppError(ErrorType.ALREADY_EXISTS, "Article with this title already exists")
        )):
            response = client.post("/api/articles", json={
                "article": {
                    "title": "Test Article",
                    "description": "A test description",
                    "body": "Test body content",
                }
            }, headers=auth_headers)

            assert response.status_code == 422
            data = response.json()
            assert "Article with this title already exists" in data["errors"]["body"][0]


class TestGetArticle:
    def test_get_article_success(self, client, sample_article):
        with patch("features.article.controller.get_article", new=AsyncMock(return_value=sample_article)):
            response = client.get("/api/articles/test-article")

            assert response.status_code == 200
            data = response.json()
            assert data["article"]["slug"] == "test-article"
            assert data["article"]["title"] == "Test Article"

    def test_get_article_with_auth(self, client, sample_article, auth_headers):
        with patch("features.article.controller.get_article", new=AsyncMock(return_value=sample_article)):
            response = client.get("/api/articles/test-article", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["article"]["slug"] == "test-article"

    def test_get_article_not_found(self, client):
        with patch("features.article.controller.get_article", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Article not found with slug: 'nonexistent'")
        )):
            response = client.get("/api/articles/nonexistent")

            assert response.status_code == 404
            data = response.json()
            assert "Article not found" in data["errors"]["body"][0]


class TestUpdateArticle:
    def test_update_article_success(self, client, sample_article, auth_headers):
        updated_article = Article(
            id=1, slug="test-article", title="Test Article",
            description="Updated description", body="Test body content",
            tag_list=["tag1", "tag2"],
            created_at=datetime(2025, 1, 1, 12, 0, 0),
            updated_at=datetime(2025, 1, 2, 12, 0, 0),
            favorited=False, favorites_count=0,
            author=Author(username="testuser", bio="Test bio", image=None, following=False),
        )

        with patch("features.article.controller.update_article", new=AsyncMock(return_value=updated_article)):
            response = client.put("/api/articles/test-article", json={
                "article": {"description": "Updated description"}
            }, headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["article"]["description"] == "Updated description"

    def test_update_article_unauthorized(self, client):
        response = client.put("/api/articles/test-article", json={
            "article": {"description": "Updated"}
        })

        assert response.status_code == 401

    def test_update_article_forbidden(self, client, auth_headers):
        with patch("features.article.controller.update_article", new=AsyncMock(
            side_effect=AppError(ErrorType.FORBIDDEN, "You are not allowed to update this article")
        )):
            response = client.put("/api/articles/test-article", json={
                "article": {"description": "Updated"}
            }, headers=auth_headers)

            assert response.status_code == 403

    def test_update_article_not_found(self, client, auth_headers):
        with patch("features.article.controller.update_article", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Article not found with slug: 'nonexistent'")
        )):
            response = client.put("/api/articles/nonexistent", json={
                "article": {"description": "Updated"}
            }, headers=auth_headers)

            assert response.status_code == 404


class TestDeleteArticle:
    def test_delete_article_success(self, client, auth_headers):
        with patch("features.article.controller.delete_article", new=AsyncMock()):
            response = client.delete("/api/articles/test-article", headers=auth_headers)

            assert response.status_code == 200

    def test_delete_article_unauthorized(self, client):
        response = client.delete("/api/articles/test-article")

        assert response.status_code == 401

    def test_delete_article_forbidden(self, client, auth_headers):
        with patch("features.article.controller.delete_article", new=AsyncMock(
            side_effect=AppError(ErrorType.FORBIDDEN, "You are not allowed to delete this article")
        )):
            response = client.delete("/api/articles/test-article", headers=auth_headers)

            assert response.status_code == 403

    def test_delete_article_not_found(self, client, auth_headers):
        with patch("features.article.controller.delete_article", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Article not found with slug: 'nonexistent'")
        )):
            response = client.delete("/api/articles/nonexistent", headers=auth_headers)

            assert response.status_code == 404


class TestFavoriteArticle:
    def test_favorite_article_success(self, client, sample_article, auth_headers):
        favorited_article = Article(
            id=1, slug="test-article", title="Test Article",
            description="A test description", body="Test body content",
            tag_list=["tag1", "tag2"],
            created_at=datetime(2025, 1, 1, 12, 0, 0),
            updated_at=datetime(2025, 1, 1, 12, 0, 0),
            favorited=True, favorites_count=1,
            author=Author(username="testuser", bio="Test bio", image=None, following=True),
        )

        with patch("features.article.controller.favorite_article", new=AsyncMock(return_value=favorited_article)):
            response = client.post("/api/articles/test-article/favorite", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["article"]["favorited"] is True
            assert data["article"]["favoritesCount"] == 1

    def test_favorite_article_unauthorized(self, client):
        response = client.post("/api/articles/test-article/favorite")

        assert response.status_code == 401

    def test_favorite_article_not_found(self, client, auth_headers):
        with patch("features.article.controller.favorite_article", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Article not found with slug: 'nonexistent'")
        )):
            response = client.post("/api/articles/nonexistent/favorite", headers=auth_headers)

            assert response.status_code == 404


class TestUnfavoriteArticle:
    def test_unfavorite_article_success(self, client, auth_headers):
        unfavorited_article = Article(
            id=1, slug="test-article", title="Test Article",
            description="A test description", body="Test body content",
            tag_list=["tag1", "tag2"],
            created_at=datetime(2025, 1, 1, 12, 0, 0),
            updated_at=datetime(2025, 1, 1, 12, 0, 0),
            favorited=False, favorites_count=0,
            author=Author(username="testuser", bio="Test bio", image=None, following=False),
        )

        with patch("features.article.controller.unfavorite_article", new=AsyncMock(return_value=unfavorited_article)):
            response = client.delete("/api/articles/test-article/favorite", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["article"]["favorited"] is False
            assert data["article"]["favoritesCount"] == 0

    def test_unfavorite_article_unauthorized(self, client):
        response = client.delete("/api/articles/test-article/favorite")

        assert response.status_code == 401

    def test_unfavorite_article_not_found(self, client, auth_headers):
        with patch("features.article.controller.unfavorite_article", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Article not found with slug: 'nonexistent'")
        )):
            response = client.delete("/api/articles/nonexistent/favorite", headers=auth_headers)

            assert response.status_code == 404


class TestGetTags:
    def test_get_tags_success(self, client):
        with patch("features.article.controller.get_tags", new=AsyncMock(return_value=["tag1", "tag2", "tag3"])):
            response = client.get("/api/tags")

            assert response.status_code == 200
            data = response.json()
            assert data["tags"] == ["tag1", "tag2", "tag3"]

    def test_get_tags_empty(self, client):
        with patch("features.article.controller.get_tags", new=AsyncMock(return_value=[])):
            response = client.get("/api/tags")

            assert response.status_code == 200
            data = response.json()
            assert data["tags"] == []