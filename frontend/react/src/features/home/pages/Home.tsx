import { useEffect, useMemo } from "react";
import { Link, useNavigate } from "@tanstack/react-router";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { type HomeSearch, Route } from "@/routes";
import Tags from "@/features/home/components/Tags.tsx";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import {
    createArticleFavorite,
    deleteArticleFavorite,
    getArticles,
    getArticlesFeed,
    type MultipleArticlesResponse,
} from "@/features/article/api/article-api.ts";
import defaultAvatar from "@/shared/assets/default-avatar.svg";

const PAGE_SIZE = 5;

function getArticleQuery(search: HomeSearch, isAuthenticated: boolean) {
    const page = search.page ?? 1;
    if (search.personal && isAuthenticated) {
        return getArticlesFeed({ offset: (page - 1) * PAGE_SIZE, limit: PAGE_SIZE });
    }
    return getArticles({ tag: search.tag, offset: (page - 1) * PAGE_SIZE, limit: PAGE_SIZE });
}

export default function Home() {
    const search = Route.useSearch() as HomeSearch;
    const { isAuthenticated } = useAuth();

    const navigate = useNavigate();
    const queryClient = useQueryClient();

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
            <div key={article.slug} className="article-preview">
                <div className="article-meta">
                    <Link to={`/profile/${article.author.username}`}>
                        <img src={article.author.image ?? defaultAvatar} alt="avatar" />
                    </Link>
                    <div className="info">
                        <Link to={`/profile/${article.author.username}`} className="author">
                            {article.author.username}
                        </Link>
                        <span className="date">
                            {new Date(article.createdAt).toLocaleDateString()}
                        </span>
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

                            queryClient.setQueryData(
                                ["articles", search.tag, search.personal, search.page],
                                (oldData: MultipleArticlesResponse) => {
                                    return {
                                        articles: oldData.articles.map((oldArticle) => {
                                            if (oldArticle.slug === article.slug) {
                                                const addCount = favorited ? 1 : -1;
                                                return {
                                                    ...oldArticle,
                                                    favorited,
                                                    favoritesCount:
                                                        oldArticle.favoritesCount + addCount,
                                                };
                                            }
                                            return oldArticle;
                                        }),
                                        articlesCount: oldData.articlesCount,
                                    };
                                },
                            );
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
                            <li
                                key={article.slug + tag}
                                className="tag-default tag-pill tag-outline"
                            >
                                {tag}
                            </li>
                        ))}
                    </ul>
                </Link>
            </div>
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
