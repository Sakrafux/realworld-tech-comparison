import type { Profile } from "@/api/features/profile-api.ts";
import { useEffect, useMemo, useState } from "preact/hooks";
import { type Article, getArticles } from "@/api/features/article-api.ts";
import ArticlePreview from "@/components/ArticlePreview.tsx";

const PAGE_SIZE = 10;

export type ProfileArticlesProps = {
    profile: Profile;
    isFavorites: boolean;
};

export default function ProfileArticles({ profile, isFavorites }: ProfileArticlesProps) {
    const [articles, setArticles] = useState<Article[]>([]);
    const [articlesCount, setArticlesCount] = useState(0);
    const [currentPage, setCurrentPage] = useState(
        Number(new URLSearchParams(window.location.search).get("page")) || 1,
    );

    useEffect(() => {
        getArticles({
            favorited: isFavorites ? profile.username : undefined,
            author: !isFavorites ? profile.username : undefined,
            offset: (currentPage - 1) * PAGE_SIZE,
            limit: PAGE_SIZE,
        }).then((response) => {
            setArticles(response.articles);
            setArticlesCount(response.articlesCount);
        });
    }, []);

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
                            window.location.search = `?page=${i}`;
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
