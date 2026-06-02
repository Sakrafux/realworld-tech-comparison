import { Link, useNavigate } from "@tanstack/react-router";
import {
    type Article,
    createArticleFavorite,
    deleteArticleFavorite,
    type MultipleArticlesResponse,
} from "@/shared/api/features/article-api.ts";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { useQueryClient } from "@tanstack/react-query";

export type ArticlePreviewProps = {
    article: Article;
    queryKey: any[];
};

export default function ArticlePreview({ article, queryKey }: ArticlePreviewProps) {
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    return (
        <div className="article-preview">
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
                <button
                    type="button"
                    className={`btn btn-outline-primary btn-sm pull-xs-right ${article.favorited ? "active" : ""}`}
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

                        queryClient.setQueryData(queryKey, (oldData: MultipleArticlesResponse) => {
                            return {
                                articles: oldData.articles.map((oldArticle) => {
                                    if (oldArticle.slug === article.slug) {
                                        const addCount = favorited ? 1 : -1;
                                        return {
                                            ...oldArticle,
                                            favorited,
                                            favoritesCount: oldArticle.favoritesCount + addCount,
                                        };
                                    }
                                    return oldArticle;
                                }),
                                articlesCount: oldData.articlesCount,
                            };
                        });
                    }}
                >
                    <i className="ion-heart"></i> {article.favoritesCount}
                </button>
            </div>
            <Link to={`/article/${article.slug}`} className="preview-link">
                <h1>{article.title}</h1>
                <p>{article.description}</p>
                <span>Read more...</span>
                <ul className="tag-list">
                    {article.tagList.map((tag) => (
                        <li key={article.slug + tag} className="tag-default tag-pill tag-outline">
                            {tag}
                        </li>
                    ))}
                </ul>
            </Link>
        </div>
    );
}
