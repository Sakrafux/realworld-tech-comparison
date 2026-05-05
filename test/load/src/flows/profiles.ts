import {sleep} from 'k6';
import {randomItem, User, getAuthHeaders} from '../utils.ts';
import {profiles} from '../groups/profiles.ts';

export default function (users: User[]) {
    const user = randomItem(users);
    const authParams = getAuthHeaders(user.token);
    profiles(authParams, users, user);
    sleep(1);
}
