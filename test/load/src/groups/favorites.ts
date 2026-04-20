import http from 'k6/http';
import {check, group} from 'k6';
import {BASE_URL} from '../utils.ts';

export function favorites(authParams: any, slug: string) {
    group('Favorites', () => {
        let res = http.post(`${BASE_URL}/articles/${slug}/favorite`, null, {
            ...authParams,
            tags: {name: 'FavoriteArticle'}
        });
        check(res, {'favorite article status 200': (r) => r.status === 200});

        res = http.del(`${BASE_URL}/articles/${slug}/favorite`, null, {
            ...authParams,
            tags: {name: 'UnfavoriteArticle'}
        });
        check(res, {'unfavorite article status 200': (r) => r.status === 200});
    });
}
