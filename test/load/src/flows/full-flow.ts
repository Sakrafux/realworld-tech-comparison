import {sleep} from 'k6';
import {randomItem, randomString, getAuthHeaders, User} from '../utils.ts';
import {articles, deleteArticle, feedsAndTags} from '../groups/articles.ts';
import {comments, deleteComment} from '../groups/comments.ts';
import {favorites} from '../groups/favorites.ts';
import {profiles} from '../groups/profiles.ts';

export default function (users: User[]) {
    const user = randomItem(users);
    const authParams = getAuthHeaders(user.token);
    const articleTitle = `Full Flow Article ${randomString(10)}`;

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
    profiles(authParams, users, user);

    sleep(1);
}
