import {sleep} from 'k6';
import {Options} from 'k6/options';
import {randomString} from './utils.ts';
import {userAndAuth} from './groups/auth.ts';
import {articles, deleteArticle, feedsAndTags} from './groups/articles.ts';
import {comments, deleteComment} from './groups/comments.ts';
import {favorites} from './groups/favorites.ts';
import {profiles} from './groups/profiles.ts';

export let options: Options = {
    vus: 10,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<500'], // Global
        'http_req_duration{name:Register}': ['p(95)<500'],
        'http_req_duration{name:Login}': ['p(95)<500'],
        'http_req_duration{name:GetCurrentUser}': ['p(95)<500'],
        'http_req_duration{name:CreateArticle}': ['p(95)<500'],
        'http_req_duration{name:GetArticle}': ['p(95)<500'],
        'http_req_duration{name:AddComment}': ['p(95)<500'],
        'http_req_duration{name:GetComments}': ['p(95)<500'],
        'http_req_duration{name:FavoriteArticle}': ['p(95)<500'],
        'http_req_duration{name:DeleteComment}': ['p(95)<500'],
        'http_req_duration{name:UnfavoriteArticle}': ['p(95)<500'],
        'http_req_duration{name:DeleteArticle}': ['p(95)<500'],
        'http_req_duration{name:GetTags}': ['p(95)<500'],
        'http_req_duration{name:GetGlobalArticles}': ['p(95)<500'],
        'http_req_duration{name:GetArticlesFeed}': ['p(95)<500'],
        'http_req_duration{name:FollowUser}': ['p(95)<500'],
        'http_req_duration{name:UnfollowUser}': ['p(95)<500'],
    },
};

export default function () {
    const username = `user_${randomString(10)}`;
    const email = `${username}@example.com`;
    const password = 'password123';
    const articleTitle = `Article ${randomString(10)}`;

    const token = userAndAuth(username, email, password);
    if (!token) return;

    const authParams = {
        headers: {'Content-Type': 'application/json', Authorization: `Token ${token}`},
    };

    const slug = articles(authParams, articleTitle);

    if (slug) {
        const commentId = comments(authParams, slug);

        favorites(authParams, slug);

        if (commentId) {
            deleteComment(authParams, slug, commentId);
        }

        deleteArticle(authParams, slug);
    }

    feedsAndTags(authParams);

    profiles(authParams, password);

    sleep(1);
}
