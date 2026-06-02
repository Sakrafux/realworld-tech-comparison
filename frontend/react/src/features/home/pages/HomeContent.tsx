import { useEffect, useMemo } from "react";
import { Link, useNavigate } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import Tags from "@/features/home/components/Tags.tsx";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { getArticles, getArticlesFeed } from "@/shared/api/features/article-api.ts";
import ArticlePreview from "@/features/article/components/ArticlePreview.tsx";

const PAGE_SIZE = 5;

export type HomeContentProps = {
    tag?: string;
    page?: number;
    feed?: string;
};

export default function HomeContent({ tag, page = 1, feed }: HomeContentProps) {
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const { data: articles } = useQuery({
        queryKey: ["articles", tag, feed, page],
        queryFn: () => {
            if (feed === "following" && isAuthenticated) {
                return getArticlesFeed({ offset: (page - 1) * PAGE_SIZE, limit: PAGE_SIZE });
            }
            return getArticles({ tag, offset: (page - 1) * PAGE_SIZE, limit: PAGE_SIZE });
        },
        initialData: () => ({ articles: [], articlesCount: 0 }),
    });

    const pageElements = useMemo(() => {
        const elements = [];
        for (let i = 1; i <= Math.ceil(articles.articlesCount / PAGE_SIZE); i++) {
            elements.push(
                <li key={`page-link-${i}`} className="page-item">
                    <Link
                        className="page-link"
                        to={tag ? "/tag/$tag" : "/"}
                        params={tag ? { tag } : undefined}
                        search={tag ? { page: i } : { feed, page: i }}
                    >
                        {i}
                    </Link>
                </li>,
            );
        }
        return elements;
    }, [articles.articlesCount, page, tag, feed]);

    const articleElements = useMemo(() => {
        return articles.articles.map((article) => (
            <ArticlePreview
                key={article.slug}
                article={article}
                queryKey={["articles", tag, feed, page]}
            />
        ));
    }, [articles.articles, tag, feed, page]);

    useEffect(() => {
        if (feed === "following" && !isAuthenticated) {
            navigate({ to: "/login" });
        }
    }, [feed, isAuthenticated]);

    return (
        <div className="home-page">
            <div className="banner">
                <div className="container">
                    <h1 className="logo-font">conduit</h1>
                    <p>A place to share your knowledge.</p>
                </div>
            </div>

            <div className="container page">
                <div className="row">
                    <div className="col-md-9">
                        <div className="feed-toggle">
                            <ul className="nav nav-pills outline-active">
                                <li className="nav-item">
                                    <Link
                                        className="nav-link"
                                        to="/"
                                        search={{ feed: "following" }}
                                    >
                                        Your Feed
                                    </Link>
                                </li>
                                <li className="nav-item">
                                    <Link
                                        className="nav-link"
                                        to="/"
                                        activeProps={{
                                            className: !tag && !feed ? "active" : "",
                                        }}
                                    >
                                        Global Feed
                                    </Link>
                                </li>
                                {tag && (
                                    <li className="nav-item">
                                        <Link
                                            className="nav-link active"
                                            to="/tag/$tag"
                                            params={{ tag }}
                                        >
                                            #{tag}
                                        </Link>
                                    </li>
                                )}
                            </ul>
                        </div>

                        {articleElements.length === 0 ? (
                            <div className="empty-feed-message">
                                Your feed is empty{" "}
                                {feed === "following" ? (
                                    <Link to="/">Back to Global Feed</Link>
                                ) : null}
                            </div>
                        ) : (
                            articleElements
                        )}

                        <ul className="pagination">{pageElements}</ul>
                    </div>

                    <div className="col-md-3">
                        <Tags />
                    </div>
                </div>
            </div>
        </div>
    );
}
