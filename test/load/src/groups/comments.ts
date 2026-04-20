import http from 'k6/http';
import {check, group} from 'k6';
import {BASE_URL} from '../utils.ts';

export function comments(authParams: any, slug: string) {
    let commentId = null;

    group('Comments', () => {
        const commentPayload = JSON.stringify({comment: {body: 'Test comment'}});
        let res = http.post(`${BASE_URL}/articles/${slug}/comments`, commentPayload, {
            ...authParams,
            tags: {name: 'AddComment'}
        });
        check(res, {'add comment status 200': (r) => r.status === 200});

        if (res.status === 200) {
            const body = res.json() as any;
            commentId = body.comment.id;
        }

        res = http.get(`${BASE_URL}/articles/${slug}/comments`, {tags: {name: 'GetComments'}});
        check(res, {'get comments status 200': (r) => r.status === 200});
    });

    return commentId;
}

export function deleteComment(authParams: any, slug: string, commentId: any) {
    group('Delete Comment', () => {
        let res = http.del(`${BASE_URL}/articles/${slug}/comments/${commentId}`, null, {
            ...authParams,
            tags: {name: 'DeleteComment'}
        });
        check(res, {'delete comment status 200': (r) => r.status === 200});
    });
}
