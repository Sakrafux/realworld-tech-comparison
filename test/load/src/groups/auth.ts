import http from 'k6/http';
import {check, group} from 'k6';
import {BASE_URL} from '../utils.ts';

export function userAndAuth(username: string, email: string, password: string) {
    let token = '';

    group('User and Authentication', () => {
        const registerPayload = JSON.stringify({
            user: {username, email, password},
        });
        let res = http.post(`${BASE_URL}/users`, registerPayload, {
            headers: {'Content-Type': 'application/json'},
            tags: {name: 'Register'},
        });
        check(res, {'register status 201': (r) => r.status === 201});

        if (res.status === 201) {
            const body = res.json() as any;
            token = body.user.token;
        } else return;

        const loginPayload = JSON.stringify({
            user: {email, password},
        });
        res = http.post(`${BASE_URL}/users/login`, loginPayload, {
            headers: {'Content-Type': 'application/json'},
            tags: {name: 'Login'},
        });
        check(res, {'login status 200': (r) => r.status === 200});

        res = http.get(`${BASE_URL}/user`, {
            headers: {'Content-Type': 'application/json', Authorization: `Token ${token}`},
            tags: {name: 'GetCurrentUser'},
        });
        check(res, {'get current user status 200': (r) => r.status === 200});
    });

    return token;
}
