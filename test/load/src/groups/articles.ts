import http from 'k6/http';
import {check, group} from 'k6';
import {BASE_URL} from '../utils.ts';

export function articles(authParams: any, articleTitle: string) {
    let slug = '';

    group('Articles', () => {
        const articlePayload = JSON.stringify({
            article: {title: articleTitle, description: 'Test', body: 'Test', tagList: ['test']},
        });
        let res = http.post(`${BASE_URL}/articles`, articlePayload, {
            ...authParams,
            tags: {name: 'CreateArticle'}
        });
        check(res, {'create article status 201': (r) => r.status === 201});

        if (res.status === 201) {
            const body = res.json() as any;
            slug = body.article.slug;
        }

        if (slug) {
            res = http.get(`${BASE_URL}/articles/${slug}`, {tags: {name: 'GetArticle'}});
            check(res, {'get article status 200': (r) => r.status === 200});
        }
    });

    return slug;
}

export function deleteArticle(authParams: any, slug: string) {
    group('Delete Article', () => {
        let res = http.del(`${BASE_URL}/articles/${slug}`, null, {
            ...authParams,
            tags: {name: 'DeleteArticle'}
        });
        check(res, {'delete article status 200': (r) => r.status === 200});
    });
}

export function feedsAndTags(authParams: any) {
    group('Global Feeds and Tags', () => {
        let res = http.get(`${BASE_URL}/tags`, {tags: {name: 'GetTags'}});
        check(res, {'get tags status 200': (r) => r.status === 200});

        res = http.get(`${BASE_URL}/articles?limit=10&offset=0`, {tags: {name: 'GetGlobalArticles'}});
        check(res, {'get global articles status 200': (r) => r.status === 200});

        res = http.get(`${BASE_URL}/articles/feed?limit=10&offset=0`, {
            ...authParams,
            tags: {name: 'GetArticlesFeed'}
        });
        check(res, {'get articles feed status 200': (r) => r.status === 200});
    });
}
