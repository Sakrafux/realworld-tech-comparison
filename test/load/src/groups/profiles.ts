import http from 'k6/http';
import {check, group} from 'k6';
import {BASE_URL, randomString} from '../utils.ts';

export function profiles(authParams: any, password: string) {
    group('Profiles', () => {
        const otherUser = `user_${randomString(10)}`;
        const registerOtherPayload = JSON.stringify({
            user: {username: otherUser, email: `${otherUser}@example.com`, password},
        });
        let res = http.post(`${BASE_URL}/users`, registerOtherPayload, {
            headers: {'Content-Type': 'application/json'},
            tags: {name: 'Register'},
        });

        if (res.status === 201) {
            res = http.post(`${BASE_URL}/profiles/${otherUser}/follow`, null, {
                ...authParams,
                tags: {name: 'FollowUser'}
            });
            check(res, {'follow user status 200': (r) => r.status === 200});

            res = http.del(`${BASE_URL}/profiles/${otherUser}/follow`, null, {
                ...authParams,
                tags: {name: 'UnfollowUser'}
            });
            check(res, {'unfollow user status 200': (r) => r.status === 200});
        }
    });
}
