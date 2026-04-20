import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Options } from 'k6/options';

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

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';

function randomString(length: number): string {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

export default function () {
  const username = `user_${randomString(10)}`;
  const email = `${username}@example.com`;
  const password = 'password123';
  const articleTitle = `Article ${randomString(10)}`;
  let token = '';
  let slug = '';
  let commentId = null;

  group('User and Authentication', () => {
    const registerPayload = JSON.stringify({
      user: { username, email, password },
    });
    let res = http.post(`${BASE_URL}/users`, registerPayload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Register' },
    });
    check(res, { 'register status 201': (r) => r.status === 201 });

    if (res.status === 201) {
      const body = res.json() as any;
      token = body.user.token;
    } else return;

    const loginPayload = JSON.stringify({
      user: { email, password },
    });
    res = http.post(`${BASE_URL}/users/login`, loginPayload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Login' },
    });
    check(res, { 'login status 200': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/user`, {
      headers: { 'Content-Type': 'application/json', Authorization: `Token ${token}` },
      tags: { name: 'GetCurrentUser' },
    });
    check(res, { 'get current user status 200': (r) => r.status === 200 });
  });

  const authParams = {
    headers: { 'Content-Type': 'application/json', Authorization: `Token ${token}` },
  };

  group('Articles', () => {
    const articlePayload = JSON.stringify({
      article: { title: articleTitle, description: 'Test', body: 'Test', tagList: ['test'] },
    });
    let res = http.post(`${BASE_URL}/articles`, articlePayload, { ...authParams, tags: { name: 'CreateArticle' } });
    check(res, { 'create article status 201': (r) => r.status === 201 });

    if (res.status === 201) {
      const body = res.json() as any;
      slug = body.article.slug;
    }

    if (slug) {
      res = http.get(`${BASE_URL}/articles/${slug}`, { tags: { name: 'GetArticle' } });
      check(res, { 'get article status 200': (r) => r.status === 200 });
    }
  });

  if (slug) {
    group('Comments', () => {
      const commentPayload = JSON.stringify({ comment: { body: 'Test comment' } });
      let res = http.post(`${BASE_URL}/articles/${slug}/comments`, commentPayload, { ...authParams, tags: { name: 'AddComment' } });
      check(res, { 'add comment status 200': (r) => r.status === 200 });

      if (res.status === 200) {
        const body = res.json() as any;
        commentId = body.comment.id;
      }

      res = http.get(`${BASE_URL}/articles/${slug}/comments`, { tags: { name: 'GetComments' } });
      check(res, { 'get comments status 200': (r) => r.status === 200 });
    });

    group('Favorites', () => {
      let res = http.post(`${BASE_URL}/articles/${slug}/favorite`, null, { ...authParams, tags: { name: 'FavoriteArticle' } });
      check(res, { 'favorite article status 200': (r) => r.status === 200 });

      res = http.del(`${BASE_URL}/articles/${slug}/favorite`, null, { ...authParams, tags: { name: 'UnfavoriteArticle' } });
      check(res, { 'unfavorite article status 200': (r) => r.status === 200 });
    });

    if (commentId) {
      group('Delete Comment', () => {
        let res = http.del(`${BASE_URL}/articles/${slug}/comments/${commentId}`, null, { ...authParams, tags: { name: 'DeleteComment' } });
        check(res, { 'delete comment status 200': (r) => r.status === 200 });
      });
    }

    group('Delete Article', () => {
      let res = http.del(`${BASE_URL}/articles/${slug}`, null, { ...authParams, tags: { name: 'DeleteArticle' } });
      check(res, { 'delete article status 200': (r) => r.status === 200 });
    });
  }

  group('Global Feeds and Tags', () => {
    let res = http.get(`${BASE_URL}/tags`, { tags: { name: 'GetTags' } });
    check(res, { 'get tags status 200': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/articles?limit=10&offset=0`, { tags: { name: 'GetGlobalArticles' } });
    check(res, { 'get global articles status 200': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/articles/feed?limit=10&offset=0`, { ...authParams, tags: { name: 'GetArticlesFeed' } });
    check(res, { 'get articles feed status 200': (r) => r.status === 200 });
  });

  group('Profiles', () => {
    const otherUser = `user_${randomString(10)}`;
    const registerOtherPayload = JSON.stringify({
      user: { username: otherUser, email: `${otherUser}@example.com`, password },
    });
    let res = http.post(`${BASE_URL}/users`, registerOtherPayload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Register' },
    });

    if (res.status === 201) {
      res = http.post(`${BASE_URL}/profiles/${otherUser}/follow`, null, { ...authParams, tags: { name: 'FollowUser' } });
      check(res, { 'follow user status 200': (r) => r.status === 200 });

      res = http.del(`${BASE_URL}/profiles/${otherUser}/follow`, null, { ...authParams, tags: { name: 'UnfollowUser' } });
      check(res, { 'unfollow user status 200': (r) => r.status === 200 });
    }
  });

  sleep(1);
}
