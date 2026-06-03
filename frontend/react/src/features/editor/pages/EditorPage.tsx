import { useState, type KeyboardEvent, type SubmitEvent, useEffect } from "react";
import { useNavigate, useParams } from "@tanstack/react-router";
import { createArticle, getArticle, updateArticle } from "@/shared/api/features/article-api.ts";
import { useQuery, useQueryClient } from "@tanstack/react-query";

export default function EditorPage() {
    // State for the core article fields
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [body, setBody] = useState("");

    // State for managing tags
    const [currentTag, setCurrentTag] = useState("");
    const [tagList, setTagList] = useState<string[]>([]);

    const [error, setError] = useState<string>();

    const { slug } = useParams({ strict: false }) as { slug?: string };

    const {
        data: articleData,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ["article", slug!],
        queryFn: () => getArticle(slug!),
        enabled: slug !== undefined,
        retry: false,
    });

    const article = articleData?.article;

    const navigate = useNavigate();
    const queryClient = useQueryClient();

    useEffect(() => {
        if (article) {
            setTitle(article.title || "");
            setDescription(article.description || "");
            setBody(article.body || "");
        }
    }, [article]);

    useEffect(() => {
        if (isError) {
            navigate({ to: "/" });
        }
    }, [isError]);

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            e.preventDefault(); // Prevent accidental form submission
            const trimmedTag = currentTag.trim();

            // Add the tag if it's not empty and not a duplicate
            if (trimmedTag && !tagList.includes(trimmedTag)) {
                setTagList([...tagList, trimmedTag]);
                setCurrentTag("");
            }
        }
    };

    const removeTag = (tagToRemove: string) => {
        setTagList(tagList.filter((tag) => tag !== tagToRemove));
    };

    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(undefined);

        const articleData = {
            title,
            description,
            body,
            tagList,
        };

        try {
            if (slug) {
                const result = await updateArticle(slug, articleData);
                queryClient.setQueryData(["article", result.article.slug], () => result);
                await navigate({ to: `/article/${result.article.slug}` });
            } else {
                const result = await createArticle(articleData);
                queryClient.setQueryData(["article", result.article.slug], () => result);
                await navigate({ to: `/article/${result.article.slug}` });
            }
        } catch (err) {
            setError((err as Error).message || "Error at user update");
        }
    };

    if (isLoading) {
        return null;
    }

    return (
        <div className="editor-page">
            <div className="container page">
                <div className="row">
                    <div className="col-md-10 offset-md-1 col-xs-12">
                        {error && (
                            <ul className="error-messages">
                                <li>{error}</li>
                            </ul>
                        )}

                        <form onSubmit={handleSubmit}>
                            <fieldset>
                                <fieldset className="form-group">
                                    <input
                                        type="text"
                                        name="title"
                                        className="form-control form-control-lg"
                                        placeholder="Article Title"
                                        value={title}
                                        onChange={(e) => setTitle(e.target.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <input
                                        type="text"
                                        name="description"
                                        className="form-control"
                                        placeholder="What's this article about?"
                                        value={description}
                                        onChange={(e) => setDescription(e.target.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <textarea
                                        className="form-control"
                                        name="body"
                                        rows={8}
                                        placeholder="Write your article (in markdown)"
                                        value={body}
                                        onChange={(e) => setBody(e.target.value)}
                                    ></textarea>
                                </fieldset>
                                {!slug && (
                                    <fieldset className="form-group">
                                        <input
                                            type="text"
                                            className="form-control"
                                            placeholder="Enter tags"
                                            value={currentTag}
                                            onChange={(e) => setCurrentTag(e.target.value)}
                                            onKeyDown={handleKeyDown}
                                        />
                                        <div className="tag-list">
                                            {tagList.map((tag) => (
                                                <span key={tag} className="tag-default tag-pill">
                                                    <i
                                                        className="ion-close-round"
                                                        onClick={() => removeTag(tag)}
                                                        style={{
                                                            cursor: "pointer",
                                                            marginRight: "3px",
                                                        }}
                                                    ></i>{" "}
                                                    {tag}
                                                </span>
                                            ))}
                                        </div>
                                    </fieldset>
                                )}
                                <button
                                    className="btn btn-lg pull-xs-right btn-primary"
                                    type="submit"
                                >
                                    Publish Article
                                </button>
                            </fieldset>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}
