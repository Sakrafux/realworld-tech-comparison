import pytest
from datetime import datetime
from unittest.mock import patch, AsyncMock

import jwt
from fastapi.testclient import TestClient

from features.comment.domain import Comment, CommentAuthor
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


class TestCreateComment:
    def test_create_comment_success(self, client, sample_comment, auth_headers):
        with patch("features.comment.controller.create_comment", new=AsyncMock(return_value=sample_comment)):
            response = client.post("/api/articles/test-article/comments", json={
                "comment": {"body": "Test comment body"}
            }, headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["comment"]["body"] == "Test comment body"
            assert data["comment"]["id"] == 1
            assert data["comment"]["author"]["username"] == "testuser"

    def test_create_comment_unauthorized(self, client):
        response = client.post("/api/articles/test-article/comments", json={
            "comment": {"body": "Test comment body"}
        })

        assert response.status_code == 401

    def test_create_comment_validation_error_empty_body(self, client, auth_headers):
        response = client.post("/api/articles/test-article/comments", json={
            "comment": {"body": ""}
        }, headers=auth_headers)

        assert response.status_code == 422

    def test_create_comment_article_not_found(self, client, auth_headers):
        with patch("features.comment.controller.create_comment", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Article not found with slug: 'nonexistent'")
        )):
            response = client.post("/api/articles/nonexistent/comments", json={
                "comment": {"body": "Test comment body"}
            }, headers=auth_headers)

            assert response.status_code == 404


class TestGetComments:
    def test_get_comments_success(self, client, sample_comment):
        with patch("features.comment.controller.get_comments", new=AsyncMock(return_value=[sample_comment])):
            response = client.get("/api/articles/test-article/comments")

            assert response.status_code == 200
            data = response.json()
            assert len(data["comments"]) == 1
            assert data["comments"][0]["body"] == "Test comment body"

    def test_get_comments_empty(self, client):
        with patch("features.comment.controller.get_comments", new=AsyncMock(return_value=[])):
            response = client.get("/api/articles/test-article/comments")

            assert response.status_code == 200
            data = response.json()
            assert data["comments"] == []

    def test_get_comments_with_auth(self, client, auth_headers):
        comment = Comment(
            id=1, created_at=datetime(2025, 1, 1), updated_at=datetime(2025, 1, 1),
            body="Test", author=CommentAuthor(username="other", bio="", image=None, following=True),
        )
        with patch("features.comment.controller.get_comments", new=AsyncMock(return_value=[comment])):
            response = client.get("/api/articles/test-article/comments", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["comments"][0]["author"]["following"] is True

    def test_get_comments_article_not_found(self, client):
        with patch("features.comment.controller.get_comments", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Article not found with slug: 'nonexistent'")
        )):
            response = client.get("/api/articles/nonexistent/comments")

            assert response.status_code == 404


class TestDeleteComment:
    def test_delete_comment_success(self, client, auth_headers):
        with patch("features.comment.controller.delete_comment", new=AsyncMock()):
            response = client.delete("/api/articles/test-article/comments/1", headers=auth_headers)

            assert response.status_code == 200

    def test_delete_comment_unauthorized(self, client):
        response = client.delete("/api/articles/test-article/comments/1")

        assert response.status_code == 401

    def test_delete_comment_not_found(self, client, auth_headers):
        with patch("features.comment.controller.delete_comment", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Comment not found with id: '999'")
        )):
            response = client.delete("/api/articles/test-article/comments/999", headers=auth_headers)

            assert response.status_code == 404

    def test_delete_comment_forbidden(self, client, auth_headers):
        with patch("features.comment.controller.delete_comment", new=AsyncMock(
            side_effect=AppError(ErrorType.FORBIDDEN, "You are not the author of this comment")
        )):
            response = client.delete("/api/articles/test-article/comments/1", headers=auth_headers)

            assert response.status_code == 403