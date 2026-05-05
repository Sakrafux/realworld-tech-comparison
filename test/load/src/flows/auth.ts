import {sleep} from 'k6';
import {randomItem, User} from '../utils.ts';
import {loginAndCheck} from '../groups/auth.ts';

export default function (users: User[]) {
    const user = randomItem(users);
    loginAndCheck(user.email, 'password123');
    sleep(1);
}
