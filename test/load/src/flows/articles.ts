import {sleep} from 'k6';
import {randomItem, randomString, getAuthHeaders, User} from '../utils.ts';
import {articles, deleteArticle, feedsAndTags} from '../groups/articles.ts';
import {favorites} from '../groups/favorites.ts';

export default function (users: User[]) {
    const user = randomItem(users);
    const authParams = getAuthHeaders(user.token);
    const articleTitle = `Article ${randomString(10)}`;

    const slug = articles(authParams, articleTitle);
    if (slug) {
        favorites(authParams, slug);
        deleteArticle(authParams, slug);
    }

    feedsAndTags(authParams);
    sleep(1);
}
