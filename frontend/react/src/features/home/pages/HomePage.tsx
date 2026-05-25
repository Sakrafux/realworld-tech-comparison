import { useEffect, useMemo } from "react";
import { Link, useNavigate } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { type HomeSearch, Route } from "@/routes";
import Tags from "@/features/home/components/Tags.tsx";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { getArticles, getArticlesFeed } from "@/features/article/api/article-api.ts";
import ArticlePreview from "@/features/article/components/ArticlePreview.tsx";

const PAGE_SIZE = 5;

function getArticleQuery(search: HomeSearch, isAuthenticated: boolean) {
    const page = search.page ?? 1;
    if (search.personal && isAuthenticated) {
        return getArticlesFeed({ offset: (page - 1) * PAGE_SIZE, limit: PAGE_SIZE });
    }
    return getArticles({ tag: search.tag, offset: (page - 1) * PAGE_SIZE, limit: PAGE_SIZE });
}

export default function HomePage() {
    const search = Route.useSearch() as HomeSearch;
    const { isAuthenticated } = useAuth();

    const navigate = useNavigate();

    const { data: articles } = useQuery({
        queryKey: ["articles", search.tag, search.personal, search.page],
        queryFn: () => getArticleQuery(search, isAuthenticated),
        initialData: () => ({ articles: [], articlesCount: 0 }),
    });

    const pageElements = useMemo(() => {
        const elements = [];
        for (let i = 1; i <= Math.ceil(articles.articlesCount / PAGE_SIZE); i++) {
            elements.push(
                <li key={`page-link-${i}`} className="page-item">
                    <Link
                        className="page-link"
                        to="."
                        search={(prev: HomeSearch) => ({ ...prev, page: i }) as HomeSearch}
                    >
                        {i}
                    </Link>
                </li>,
            );
        }
        return elements;
    }, [articles.articlesCount, search.page]);

    const articleElements = useMemo(() => {
        return articles.articles.map((article) => (
            <ArticlePreview
                key={article.slug}
                article={article}
                queryKey={["articles", search.tag, search.personal, search.page]}
            />
        ));
    }, [articles.articles]);

    useEffect(() => {
        if (search.personal && !isAuthenticated) {
            navigate({ to: "/login" });
        }
    }, [search.personal, isAuthenticated]);

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
                                        to="."
                                        search={{ personal: true } as HomeSearch}
                                    >
                                        Your Feed
                                    </Link>
                                </li>
                                <li className="nav-item">
                                    <Link
                                        className="nav-link"
                                        to="."
                                        activeProps={{
                                            className:
                                                search.tag === undefined &&
                                                search.personal === undefined
                                                    ? "active"
                                                    : "",
                                        }}
                                    >
                                        Global Feed
                                    </Link>
                                </li>
                                {search.tag && (
                                    <li className="nav-item">
                                        <Link
                                            className="nav-link"
                                            to="."
                                            search={{ tag: search.tag } as HomeSearch}
                                        >
                                            #{search.tag}
                                        </Link>
                                    </li>
                                )}
                            </ul>
                        </div>

                        {articleElements}

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
