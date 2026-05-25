import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { useQuery } from "@tanstack/react-query";
import { getArticle } from "@/shared/api/features/article-api.ts";
import { Route } from "@/routes/article.$slug.tsx";
import ArticleMeta from "@/features/article/components/ArticleMeta.tsx";
import Markdown from "react-markdown";
import { useEffect } from "react";
import { useNavigate } from "@tanstack/react-router";
import Comments from "@/features/article/components/Comments.tsx";

export default function ArticlePage() {
    const { username: currentUsername, isAuthenticated } = useAuth();
    const { slug } = Route.useParams() as { slug: string };

    const {
        data: articleData,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ["article", slug],
        queryFn: () => getArticle(slug),
        retry: false,
    });
    const article = articleData?.article;

    const isAuthor = isAuthenticated && article?.author.username === currentUsername;

    const navigate = useNavigate();

    useEffect(() => {
        if (isError) {
            navigate({ to: "/" });
        }
    }, [isError]);

    if (isLoading || !article) {
        return null;
    }

    return (
        <div className="article-page">
            <div className="banner">
                <div className="container">
                    <h1>{article.title}</h1>

                    <ArticleMeta article={article} slug={slug} isAuthor={isAuthor} />
                </div>
            </div>

            <div className="container page">
                <div className="row article-content">
                    <div className="col-md-12">
                        <Markdown>{article.body}</Markdown>
                        <ul className="tag-list">
                            {article.tagList.map((tag) => (
                                <li key={tag} className="tag-default tag-pill tag-outline">
                                    {tag}
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>

                <hr />

                <div className="article-actions">
                    <ArticleMeta article={article} slug={slug} isAuthor={isAuthor} />
                </div>

                <Comments slug={slug} isAuthor={isAuthor} />
            </div>
        </div>
    );
}
