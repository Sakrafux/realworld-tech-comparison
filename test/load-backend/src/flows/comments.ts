import { sleep } from "k6";
import { randomItem, randomString, getAuthHeaders, User } from "../utils.ts";
import { articles, deleteArticle } from "../groups/articles.ts";
import { comments, deleteComment } from "../groups/comments.ts";

export default function (users: User[]) {
    const user = randomItem(users);
    const authParams = getAuthHeaders(user.token);
    const articleTitle = `Article for Comment ${randomString(10)}`;

    const slug = articles(authParams, articleTitle);
    if (slug) {
        const commentId = comments(authParams, slug);
        if (commentId) {
            deleteComment(authParams, slug, commentId);
        }
        deleteArticle(authParams, slug);
    }
    sleep(1);
}
