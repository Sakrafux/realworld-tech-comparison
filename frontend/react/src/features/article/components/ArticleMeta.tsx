import { Link, useNavigate } from "@tanstack/react-router";
import {
    type Article,
    createArticleFavorite,
    deleteArticle,
    deleteArticleFavorite,
    type SingleArticleResponse,
} from "@/shared/api/features/article-api.ts";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { followUserByUsername, unfollowUserByUsername } from "@/shared/api/features/profile-api.ts";

export type ArticleMetaProps = {
    slug: string;
    article: Article;
    isAuthor: boolean;
};

export default function ArticleMeta({ article, isAuthor, slug }: ArticleMetaProps) {
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    return (
        <div className="article-meta">
            <Link to={`/profile/${article.author.username}`}>
                <img src={article.author.image || "/default-avatar.svg"} alt="avatar" />
            </Link>
            <div className="info">
                <Link to={`/profile/${article.author.username}`} className="author">
                    {article.author.username}
                </Link>
                <span className="date">{new Date(article.createdAt).toLocaleDateString()}</span>
            </div>
            {isAuthor ? (
                <>
                    <Link to={`/editor/${slug}`} className="btn btn-sm btn-outline-secondary">
                        <i className="ion-edit"></i> Edit Article
                    </Link>
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={async () => {
                            await deleteArticle(slug);
                            await navigate({ to: "/" });
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
                                await navigate({ to: "/login" });
                                return;
                            }

                            let following = false;
                            if (article.author.following) {
                                await unfollowUserByUsername(article.author.username);
                            } else {
                                await followUserByUsername(article.author.username);
                                following = true;
                            }

                            queryClient.setQueryData(
                                ["article", slug],
                                (oldData: SingleArticleResponse) => {
                                    return {
                                        article: {
                                            ...oldData.article,
                                            author: { ...oldData.article.author, following },
                                        },
                                    };
                                },
                            );
                        }}
                    >
                        {article.author.following ? (
                            <>
                                <i className="ion-minus-round"></i>
                                &nbsp; Unfollow {article.author.username}
                            </>
                        ) : (
                            <>
                                <i className="ion-plus-round"></i>
                                &nbsp; Follow {article.author.username}
                            </>
                        )}
                    </button>
                    &nbsp;&nbsp;
                    <button
                        className={`btn btn-sm ${article.favorited ? "btn-primary" : "btn-outline-primary"}`}
                        onClick={async () => {
                            if (!isAuthenticated) {
                                await navigate({ to: "/login" });
                                return;
                            }

                            let favorited = false;
                            if (article.favorited) {
                                await deleteArticleFavorite(article.slug);
                            } else {
                                await createArticleFavorite(article.slug);
                                favorited = true;
                            }

                            queryClient.setQueryData(
                                ["article", slug],
                                (oldData: SingleArticleResponse) => {
                                    return {
                                        article: {
                                            ...oldData.article,
                                            favorited: favorited,
                                            favoritesCount:
                                                oldData.article.favoritesCount +
                                                (favorited ? 1 : -1),
                                        },
                                    };
                                },
                            );
                        }}
                    >
                        <i className="ion-heart"></i>
                        &nbsp; {article.favorited ? "Unfavorite" : "Favorite"} Article{" "}
                        <span className="counter">({article.favoritesCount})</span>
                    </button>
                </>
            )}
        </div>
    );
}
