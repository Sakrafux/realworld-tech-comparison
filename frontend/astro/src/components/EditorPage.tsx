import { type Article, createArticle, updateArticle } from "@/api/features/article-api.ts";
import { useState } from "preact/hooks";
import type { TargetedKeyboardEvent, TargetedSubmitEvent } from "preact";
import { navigate } from "astro:transitions/client";

export type EditorPageProps = {
    slug: string;
    article?: Article;
};

export default function EditorPage({ slug, article }: EditorPageProps) {
    const [title, setTitle] = useState(article?.title ?? "");
    const [description, setDescription] = useState(article?.description ?? "");
    const [body, setBody] = useState(article?.body ?? "");

    const [currentTag, setCurrentTag] = useState("");
    const [tagList, setTagList] = useState<string[]>([]);

    const [error, setError] = useState<string>();

    const handleKeyDown = (e: TargetedKeyboardEvent<HTMLInputElement>) => {
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

    const handleSubmit = async (e: TargetedSubmitEvent<HTMLFormElement>) => {
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
                await navigate(`/article/${result.article.slug}`);
            } else {
                const result = await createArticle(articleData);
                await navigate(`/article/${result.article.slug}`);
            }
        } catch (err) {
            setError((err as Error).message || "Error at user update");
        }
    };

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
                                        onChange={(e) => setTitle(e.currentTarget.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <input
                                        type="text"
                                        name="description"
                                        className="form-control"
                                        placeholder="What's this article about?"
                                        value={description}
                                        onChange={(e) => setDescription(e.currentTarget.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <textarea
                                        className="form-control"
                                        name="body"
                                        rows={8}
                                        placeholder="Write your article (in markdown)"
                                        value={body}
                                        onChange={(e) => setBody(e.currentTarget.value)}
                                    ></textarea>
                                </fieldset>
                                {!slug && (
                                    <fieldset className="form-group">
                                        <input
                                            type="text"
                                            className="form-control"
                                            placeholder="Enter tags"
                                            value={currentTag}
                                            onChange={(e) => setCurrentTag(e.currentTarget.value)}
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
