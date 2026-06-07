import {
    type Article,
    createArticleFavorite,
    deleteArticleFavorite,
} from "@/api/features/article-api.ts";
import { isAuthenticated } from "@/util/auth-util.ts";
import { navigate } from "astro:transitions/client";
import { useState } from "preact/hooks";

export type ArticlePreviewProps = {
    article: Article;
};

export default function ArticlePreview({ article }: ArticlePreviewProps) {
    const [articleState, setArticleState] = useState(article);

    return (
        <div className="article-preview">
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
                <button
                    type="button"
                    className={`btn btn-outline-primary btn-sm pull-xs-right ${articleState.favorited ? "active" : ""}`}
                    onClick={async () => {
                        if (!isAuthenticated()) {
                            navigate("/login");
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
                    <i className="ion-heart"></i> {articleState.favoritesCount}
                </button>
            </div>
            <a href={`/article/${articleState.slug}`} className="preview-link">
                <h1>{articleState.title}</h1>
                <p>{articleState.description}</p>
                <span>Read more...</span>
                <ul className="tag-list">
                    {articleState.tagList.map((tag) => (
                        <li
                            key={articleState.slug + tag}
                            className="tag-default tag-pill tag-outline"
                        >
                            {tag}
                        </li>
                    ))}
                </ul>
            </a>
        </div>
    );
}
