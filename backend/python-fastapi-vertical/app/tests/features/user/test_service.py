import pytest
from unittest.mock import patch, AsyncMock

from features.user.domain import User, Profile
from features.user import service
from shared.errors.app_error import ErrorType, AppError


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


@pytest.mark.asyncio
async def test_register_success():
    with patch("features.user.service.find_by_email", new=AsyncMock(return_value=None)), \
         patch("features.user.service.find_by_username", new=AsyncMock(return_value=None)), \
         patch("features.user.service.hash_password", return_value="hashed_pw"), \
         patch("features.user.service.create", new=AsyncMock()) as mock_create:

        user = await service.register("newuser", "new@example.com", "password123")

        assert user.username == "newuser"
        assert user.email == "new@example.com"
        assert user.password == "hashed_pw"
        mock_create.assert_called_once()


@pytest.mark.asyncio
async def test_register_duplicate_email():
    with patch("features.user.service.find_by_email", new=AsyncMock(return_value=User(id=1, username="existing", email="test@example.com", password="pw"))):
        with pytest.raises(AppError) as exc_info:
            await service.register("newuser", "test@example.com", "password123")

        assert exc_info.value.error_type == ErrorType.ALREADY_EXISTS
        assert "Email already exists" in exc_info.value.message


@pytest.mark.asyncio
async def test_register_duplicate_username():
    with patch("features.user.service.find_by_email", new=AsyncMock(return_value=None)), \
         patch("features.user.service.find_by_username", new=AsyncMock(return_value=User(id=1, username="existing", email="other@example.com", password="pw"))):

        with pytest.raises(AppError) as exc_info:
            await service.register("existing", "new@example.com", "password123")

        assert exc_info.value.error_type == ErrorType.ALREADY_EXISTS
        assert "Username already exists" in exc_info.value.message


@pytest.mark.asyncio
async def test_login_success():
    user = User(id=1, username="testuser", email="test@example.com", password="hashed_pw")
    with patch("features.user.service.find_by_email", new=AsyncMock(return_value=user)), \
         patch("features.user.service.compare_password", new=AsyncMock()):

        result = await service.login("test@example.com", "password123")

        assert result.id == 1
        assert result.email == "test@example.com"


@pytest.mark.asyncio
async def test_login_user_not_found():
    with patch("features.user.service.find_by_email", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.login("nonexistent@example.com", "password123")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "User not found" in exc_info.value.message


@pytest.mark.asyncio
async def test_login_invalid_password():
    user = User(id=1, username="testuser", email="test@example.com", password="hashed_pw")
    with patch("features.user.service.find_by_email", new=AsyncMock(return_value=user)), \
         patch("features.user.service.compare_password", new=AsyncMock(side_effect=AppError(ErrorType.INVALID_CREDENTIALS, "Invalid email or password"))):

        with pytest.raises(AppError) as exc_info:
            await service.login("test@example.com", "wrongpassword")

        assert exc_info.value.error_type == ErrorType.INVALID_CREDENTIALS


@pytest.mark.asyncio
async def test_get_user_success():
    user = User(id=1, username="testuser", email="test@example.com", password="hashed_pw")
    with patch("features.user.service.find_by_id", new=AsyncMock(return_value=user)):
        result = await service.get_user(1)

        assert result.id == 1
        assert result.username == "testuser"


@pytest.mark.asyncio
async def test_get_user_not_found():
    with patch("features.user.service.find_by_id", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.get_user(999)

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "User not found with id: '999'" in exc_info.value.message


@pytest.mark.asyncio
async def test_get_user_by_username_success():
    user = User(id=1, username="testuser", email="test@example.com", password="hashed_pw")
    with patch("features.user.service.find_by_username", new=AsyncMock(return_value=user)):
        result = await service.get_user_by_username("testuser")

        assert result.username == "testuser"


@pytest.mark.asyncio
async def test_get_user_by_username_not_found():
    with patch("features.user.service.find_by_username", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.get_user_by_username("nonexistent")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "User not found with username: 'nonexistent'" in exc_info.value.message


@pytest.mark.asyncio
async def test_update_user_success():
    existing_user = User(id=1, username="oldname", email="old@example.com", password="old_hashed", bio="old bio")
    with patch("features.user.service.get_user", new=AsyncMock(return_value=existing_user)), \
         patch("features.user.service.find_by_email", new=AsyncMock(return_value=None)), \
         patch("features.user.service.find_by_username", new=AsyncMock(return_value=None)), \
         patch("features.user.service.hash_password", return_value="new_hashed"), \
         patch("features.user.service.update", new=AsyncMock()) as mock_update:

        result = await service.update_user(1, username="newname", email="new@example.com", password="newpass", bio="new bio")

        assert result.username == "newname"
        assert result.email == "new@example.com"
        assert result.password == "new_hashed"
        assert result.bio == "new bio"
        mock_update.assert_called_once()


@pytest.mark.asyncio
async def test_update_user_duplicate_email():
    existing_user = User(id=1, username="testuser", email="old@example.com", password="hashed")
    with patch("features.user.service.get_user", new=AsyncMock(return_value=existing_user)), \
         patch("features.user.service.find_by_email", new=AsyncMock(return_value=User(id=2, username="other", email="new@example.com", password="pw"))):

        with pytest.raises(AppError) as exc_info:
            await service.update_user(1, email="new@example.com")

        assert exc_info.value.error_type == ErrorType.ALREADY_EXISTS
        assert "Email already exists" in exc_info.value.message


@pytest.mark.asyncio
async def test_update_user_duplicate_username():
    existing_user = User(id=1, username="oldname", email="test@example.com", password="hashed")
    with patch("features.user.service.get_user", new=AsyncMock(return_value=existing_user)), \
         patch("features.user.service.find_by_email", new=AsyncMock(return_value=None)), \
         patch("features.user.service.find_by_username", new=AsyncMock(return_value=User(id=2, username="newname", email="other@example.com", password="pw"))):

        with pytest.raises(AppError) as exc_info:
            await service.update_user(1, username="newname")

        assert exc_info.value.error_type == ErrorType.ALREADY_EXISTS
        assert "Username already exists" in exc_info.value.message


@pytest.mark.asyncio
async def test_update_user_skip_email_check_when_same():
    existing_user = User(id=1, username="testuser", email="test@example.com", password="hashed")
    with patch("features.user.service.get_user", new=AsyncMock(return_value=existing_user)), \
         patch("features.user.service.find_by_username", new=AsyncMock(return_value=None)), \
         patch("features.user.service.find_by_email", new=AsyncMock()) as mock_find_email, \
         patch("features.user.service.update", new=AsyncMock()):

        await service.update_user(1, email="test@example.com")

        mock_find_email.assert_not_called()


@pytest.mark.asyncio
async def test_update_user_skip_username_check_when_same():
    existing_user = User(id=1, username="testuser", email="test@example.com", password="hashed")
    with patch("features.user.service.get_user", new=AsyncMock(return_value=existing_user)), \
         patch("features.user.service.find_by_email", new=AsyncMock(return_value=None)), \
         patch("features.user.service.find_by_username", new=AsyncMock()) as mock_find_username, \
         patch("features.user.service.update", new=AsyncMock()):

        await service.update_user(1, username="testuser")

        mock_find_username.assert_not_called()


@pytest.mark.asyncio
async def test_get_profile_success():
    profile = Profile(username="testuser", bio="Test bio", image=None, following=False)
    with patch("features.user.service.get_profile_by_username", new=AsyncMock(return_value=profile)):
        result = await service.get_profile("testuser")

        assert result.username == "testuser"
        assert result.bio == "Test bio"
        assert result.following is False


@pytest.mark.asyncio
async def test_get_profile_with_observer():
    profile = Profile(username="testuser", bio="Test bio", image=None, following=True)
    with patch("features.user.service.get_profile_by_username", new=AsyncMock(return_value=profile)):
        result = await service.get_profile("testuser", observer_id=1)

        assert result.following is True


@pytest.mark.asyncio
async def test_get_profile_not_found():
    with patch("features.user.service.get_profile_by_username", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.get_profile("nonexistent")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "Profile not found with username: 'nonexistent'" in exc_info.value.message


@pytest.mark.asyncio
async def test_follow_user_success():
    target_user = User(id=2, username="targetuser", email="target@example.com", password="hashed")
    profile = Profile(username="targetuser", bio="", image=None, following=True)
    with patch("features.user.service.find_by_username", new=AsyncMock(return_value=target_user)), \
         patch("features.user.service.follow", new=AsyncMock()) as mock_follow, \
         patch("features.user.service.get_profile_by_username", new=AsyncMock(return_value=profile)):

        result = await service.follow_user(1, "targetuser")

        mock_follow.assert_called_once_with(1, 2)
        assert result.following is True


@pytest.mark.asyncio
async def test_follow_user_not_found():
    with patch("features.user.service.find_by_username", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.follow_user(1, "nonexistent")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "User not found with username: 'nonexistent'" in exc_info.value.message


@pytest.mark.asyncio
async def test_unfollow_user_success():
    target_user = User(id=2, username="targetuser", email="target@example.com", password="hashed")
    profile = Profile(username="targetuser", bio="", image=None, following=False)
    with patch("features.user.service.find_by_username", new=AsyncMock(return_value=target_user)), \
         patch("features.user.service.unfollow", new=AsyncMock()) as mock_unfollow, \
         patch("features.user.service.get_profile_by_username", new=AsyncMock(return_value=profile)):

        result = await service.unfollow_user(1, "targetuser")

        mock_unfollow.assert_called_once_with(1, 2)
        assert result.following is False


@pytest.mark.asyncio
async def test_unfollow_user_not_found():
    with patch("features.user.service.find_by_username", new=AsyncMock(return_value=None)):
        with pytest.raises(AppError) as exc_info:
            await service.unfollow_user(1, "nonexistent")

        assert exc_info.value.error_type == ErrorType.NOT_FOUND
        assert "User not found with username: 'nonexistent'" in exc_info.value.message
