import { useEffect, useMemo, useState } from "preact/hooks";
import { type Article, getArticles, getArticlesFeed } from "@/api/features/article-api.ts";
import ArticlePreview from "@/components/ArticlePreview.tsx";
import { isAuthenticated } from "@/util/auth-util.ts";

const PAGE_SIZE = 10;

export type HomeArticlesProps = {
    tag?: string;
};

export default function HomeArticles({ tag }: HomeArticlesProps) {
    const [articles, setArticles] = useState<Article[]>([]);
    const [articlesCount, setArticlesCount] = useState(0);
    const [currentPage, setCurrentPage] = useState(
        Number(new URLSearchParams(window.location.search).get("page")) || 1,
    );

    useEffect(() => {
        const isFollowing = new URLSearchParams(window.location.search).get("feed") === "following";

        const getArticlesFn = isFollowing && isAuthenticated() ? getArticlesFeed : getArticles;

        getArticlesFn({
            tag,
            offset: (currentPage - 1) * PAGE_SIZE,
            limit: PAGE_SIZE,
        }).then((response) => {
            setArticles(response.articles);
            setArticlesCount(response.articlesCount);
        });
    }, [currentPage]);

    const pageElements = useMemo(() => {
        const elements = [];
        for (let i = 1; i <= Math.ceil(articlesCount / PAGE_SIZE); i++) {
            elements.push(
                <li
                    key={`page-link-${i}`}
                    className={`page-item ${currentPage === i ? "active" : ""}`}
                >
                    <button
                        className="page-link"
                        type="button"
                        onClick={() => {
                            const searchParams = new URLSearchParams(window.location.search);
                            searchParams.set("page", i.toString());
                            window.location.search = searchParams.toString();
                            setCurrentPage(i);
                        }}
                    >
                        {i}
                    </button>
                </li>,
            );
        }
        return elements;
    }, [articlesCount, currentPage]);

    const articleElements = useMemo(() => {
        return articles.map((article) => <ArticlePreview key={article.slug} article={article} />);
    }, [articles]);

    return (
        <>
            {articleElements}

            <ul class="pagination">{pageElements}</ul>
        </>
    );
}
