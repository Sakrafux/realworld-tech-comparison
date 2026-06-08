import {
    type Article,
    createArticleFavorite,
    deleteArticle,
    deleteArticleFavorite,
} from "@/api/features/article-api.ts";
import { getCurrentUser, isAuthenticated } from "@/util/auth-util.ts";
import { navigate } from "astro:transitions/client";
import { followUserByUsername, unfollowUserByUsername } from "@/api/features/profile-api.ts";
import { useState } from "preact/hooks";

export type ArticleMetaProps = {
    slug: string;
    article: Article;
};

export default function ArticleMeta({ article, slug }: ArticleMetaProps) {
    const [articleState, setArticleState] = useState<Article>(article);

    const isAuthor =
        isAuthenticated() && getCurrentUser()?.username === articleState.author.username;

    return (
        <div className="article-meta">
            <a href={`/profile/${articleState.author.username}`}>
                <img src={articleState.author.image || "/default-avatar.svg"} alt="avatar" />
            </a>
            <div className="info">
                <a href={`/profile/${articleState.author.username}`} className="author">
                    {articleState.author.username}
                </a>
                <span className="date">
                    {new Date(articleState.createdAt).toLocaleDateString()}
                </span>
            </div>
            {isAuthor ? (
                <>
                    <a href={`/editor/${slug}`} className="btn btn-sm btn-outline-secondary">
                        <i className="ion-edit"></i> Edit Article
                    </a>
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={async () => {
                            await deleteArticle(slug);
                            await navigate("/");
                        }}
                    >
                        <i className="ion-trash-a"></i> Delete Article
                    </button>
                </>
            ) : (
                <>
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-secondary"
                        onClick={async () => {
                            if (!isAuthenticated) {
                                await navigate("/login");
                                return;
                            }

                            if (articleState.author.following) {
                                await unfollowUserByUsername(articleState.author.username);
                                setArticleState({
                                    ...articleState,
                                    author: { ...articleState.author, following: false },
                                });
                            } else {
                                await followUserByUsername(articleState.author.username);
                                setArticleState({
                                    ...articleState,
                                    author: { ...articleState.author, following: true },
                                });
                            }
                        }}
                    >
                        {articleState.author.following ? (
                            <>
                                <i className="ion-minus-round"></i>
                                &nbsp; Unfollow {articleState.author.username}
                            </>
                        ) : (
                            <>
                                <i className="ion-plus-round"></i>
                                &nbsp; Follow {articleState.author.username}
                            </>
                        )}
                    </button>
                    &nbsp;&nbsp;
                    <button
                        className={`btn btn-sm ${articleState.favorited ? "btn-primary" : "btn-outline-primary"}`}
                        onClick={async () => {
                            if (!isAuthenticated) {
                                await navigate("/login");
                                return;
                            }

                            if (articleState.favorited) {
                                await deleteArticleFavorite(articleState.slug);
                                setArticleState({
                                    ...articleState,
                                    favorited: false,
                                    favoritesCount: articleState.favoritesCount - 1,
                                });
                            } else {
                                await createArticleFavorite(articleState.slug);
                                setArticleState({
                                    ...articleState,
                                    favorited: true,
                                    favoritesCount: articleState.favoritesCount + 1,
                                });
                            }
                        }}
                    >
                        <i className="ion-heart"></i>
                        &nbsp; {articleState.favorited ? "Unfavorite" : "Favorite"} Article{" "}
                        <span className="counter">({articleState.favoritesCount})</span>
                    </button>
                </>
            )}
        </div>
    );
}
