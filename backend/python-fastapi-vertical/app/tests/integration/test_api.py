import pytest
from unittest.mock import patch, AsyncMock

import jwt
from fastapi.testclient import TestClient

from features.user.domain import User, Profile
from main import app
from shared.config.env import settings


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
def sample_user():
    return User(
        id=1,
        username="testuser",
        email="test@example.com",
        password="hashed_pw",
        bio="Test bio",
        image=None,
    )


@pytest.fixture
def sample_profile():
    return Profile(
        username="testuser",
        bio="Test bio",
        image=None,
        following=False,
    )


class TestLogin:
    def test_login_success(self, client, sample_user):
        with patch("features.user.controller.login", new=AsyncMock(return_value=sample_user)):
            response = client.post("/api/users/login", json={
                "user": {"email": "test@example.com", "password": "password123"}
            })

            assert response.status_code == 200
            data = response.json()
            assert data["user"]["email"] == "test@example.com"
            assert data["user"]["username"] == "testuser"
            assert "token" in data["user"]

    def test_login_validation_error(self, client):
        response = client.post("/api/users/login", json={
            "user": {"email": "invalid-email", "password": ""}
        })

        assert response.status_code == 422
        data = response.json()
        assert "errors" in data
        assert "body" in data["errors"]

    def test_login_invalid_credentials(self, client):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.login", new=AsyncMock(
            side_effect=AppError(ErrorType.INVALID_CREDENTIALS, "Invalid email or password")
        )):
            response = client.post("/api/users/login", json={
                "user": {"email": "test@example.com", "password": "wrong"}
            })

            assert response.status_code == 401
            data = response.json()
            assert data["errors"]["body"][0] == "Invalid email or password"

    def test_login_user_not_found(self, client):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.login", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "User not found")
        )):
            response = client.post("/api/users/login", json={
                "user": {"email": "nonexistent@example.com", "password": "password123"}
            })

            assert response.status_code == 404
            data = response.json()
            assert data["errors"]["body"][0] == "User not found"


class TestRegister:
    def test_register_success(self, client, sample_user):
        with patch("features.user.controller.register", new=AsyncMock(return_value=sample_user)):
            response = client.post("/api/users", json={
                "user": {"username": "newuser", "email": "new@example.com", "password": "password123"}
            })

            assert response.status_code == 201
            data = response.json()
            assert data["user"]["email"] == "test@example.com"
            assert data["user"]["username"] == "testuser"
            assert "token" in data["user"]

    def test_register_validation_error(self, client):
        response = client.post("/api/users", json={
            "user": {"username": "ab", "email": "invalid", "password": "short"}
        })

        assert response.status_code == 422
        data = response.json()
        assert "errors" in data

    def test_register_duplicate_email(self, client):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.register", new=AsyncMock(
            side_effect=AppError(ErrorType.ALREADY_EXISTS, "Email already exists")
        )):
            response = client.post("/api/users", json={
                "user": {"username": "newuser", "email": "existing@example.com", "password": "password123"}
            })

            assert response.status_code == 422
            data = response.json()
            assert data["errors"]["body"][0] == "Email already exists"


class TestGetCurrentUser:
    def test_get_current_user_success(self, client, sample_user, auth_headers):
        with patch("features.user.controller.get_user", new=AsyncMock(return_value=sample_user)):
            response = client.get("/api/user", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["user"]["email"] == "test@example.com"
            assert data["user"]["username"] == "testuser"

    def test_get_current_user_unauthorized_no_header(self, client):
        response = client.get("/api/user")

        assert response.status_code == 401
        data = response.json()
        assert data["errors"]["body"][0] == "Authorization header is required"

    def test_get_current_user_unauthorized_invalid_format(self, client):
        response = client.get("/api/user", headers={"Authorization": "Bearer invalid"})

        assert response.status_code == 401
        data = response.json()
        assert data["errors"]["body"][0] == "Invalid authorization header format"

    def test_get_current_user_unauthorized_invalid_token(self, client):
        response = client.get("/api/user", headers={"Authorization": "Token invalidtoken"})

        assert response.status_code == 401

    def test_get_current_user_not_found(self, client, auth_headers):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.get_user", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "User not found with id: '1'")
        )):
            response = client.get("/api/user", headers=auth_headers)

            assert response.status_code == 404
            data = response.json()
            assert "User not found" in data["errors"]["body"][0]


class TestUpdateUser:
    def test_update_user_success(self, client, sample_user, auth_headers):
        updated_user = User(
            id=1,
            username="updateduser",
            email="updated@example.com",
            password="hashed_pw",
            bio="Updated bio",
            image=None,
        )
        with patch("features.user.controller.update_user", new=AsyncMock(return_value=updated_user)):
            response = client.put("/api/user", json={
                "user": {"username": "updateduser", "email": "updated@example.com", "bio": "Updated bio"}
            }, headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["user"]["username"] == "updateduser"
            assert data["user"]["bio"] == "Updated bio"

    def test_update_user_unauthorized(self, client):
        response = client.put("/api/user", json={
            "user": {"username": "updateduser"}
        })

        assert response.status_code == 401

    def test_update_user_validation_error(self, client, auth_headers):
        response = client.put("/api/user", json={
            "user": {"username": "ab", "email": "invalid"}
        }, headers=auth_headers)

        assert response.status_code == 422

    def test_update_user_duplicate_email(self, client, auth_headers):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.update_user", new=AsyncMock(
            side_effect=AppError(ErrorType.ALREADY_EXISTS, "Email already exists")
        )):
            response = client.put("/api/user", json={
                "user": {"email": "existing@example.com"}
            }, headers=auth_headers)

            assert response.status_code == 422
            data = response.json()
            assert data["errors"]["body"][0] == "Email already exists"


class TestGetProfile:
    def test_get_profile_success(self, client, sample_profile):
        with patch("features.user.controller.get_profile", new=AsyncMock(return_value=sample_profile)):
            response = client.get("/api/profiles/testuser")

            assert response.status_code == 200
            data = response.json()
            assert data["profile"]["username"] == "testuser"
            assert data["profile"]["bio"] == "Test bio"
            assert data["profile"]["following"] is False

    def test_get_profile_with_auth(self, client, sample_profile, auth_headers):
        following_profile = Profile(
            username="testuser",
            bio="Test bio",
            image=None,
            following=True,
        )
        with patch("features.user.controller.get_profile", new=AsyncMock(return_value=following_profile)):
            response = client.get("/api/profiles/testuser", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["profile"]["following"] is True

    def test_get_profile_not_found(self, client):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.get_profile", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "Profile not found with username: 'nonexistent'")
        )):
            response = client.get("/api/profiles/nonexistent")

            assert response.status_code == 404
            data = response.json()
            assert "Profile not found" in data["errors"]["body"][0]


class TestFollowUser:
    def test_follow_user_success(self, client, auth_headers):
        profile = Profile(username="targetuser", bio="", image=None, following=True)
        with patch("features.user.controller.follow_user", new=AsyncMock(return_value=profile)):
            response = client.post("/api/profiles/targetuser/follow", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["profile"]["username"] == "targetuser"
            assert data["profile"]["following"] is True

    def test_follow_user_unauthorized(self, client):
        response = client.post("/api/profiles/targetuser/follow")

        assert response.status_code == 401

    def test_follow_user_not_found(self, client, auth_headers):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.follow_user", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "User not found with username: 'nonexistent'")
        )):
            response = client.post("/api/profiles/nonexistent/follow", headers=auth_headers)

            assert response.status_code == 404


class TestUnfollowUser:
    def test_unfollow_user_success(self, client, auth_headers):
        profile = Profile(username="targetuser", bio="", image=None, following=False)
        with patch("features.user.controller.unfollow_user", new=AsyncMock(return_value=profile)):
            response = client.delete("/api/profiles/targetuser/follow", headers=auth_headers)

            assert response.status_code == 200
            data = response.json()
            assert data["profile"]["username"] == "targetuser"
            assert data["profile"]["following"] is False

    def test_unfollow_user_unauthorized(self, client):
        response = client.delete("/api/profiles/targetuser/follow")

        assert response.status_code == 401

    def test_unfollow_user_not_found(self, client, auth_headers):
        from shared.errors.app_error import ErrorType, AppError
        with patch("features.user.controller.unfollow_user", new=AsyncMock(
            side_effect=AppError(ErrorType.NOT_FOUND, "User not found with username: 'nonexistent'")
        )):
            response = client.delete("/api/profiles/nonexistent/follow", headers=auth_headers)

            assert response.status_code == 404
