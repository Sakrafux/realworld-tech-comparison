import { useEffect, useState } from "preact/hooks";
import type { TargetedSubmitEvent } from "preact";
import {
    type Comment,
    createArticleComment,
    deleteArticleComment,
    getArticleComments,
} from "@/api/features/comment-api.ts";
import { getCurrentUser } from "@/util/auth-util.ts";

export type CommentsProps = {
    slug: string;
};

export default function Comments({ slug }: CommentsProps) {
    const [commentText, setCommentText] = useState<string>();
    const [comments, setComments] = useState<Comment[]>([]);

    const user = getCurrentUser();

    useEffect(() => {
        getArticleComments(slug).then((response) => setComments(response.comments));
    }, []);

    const handleSubmit = async (e: TargetedSubmitEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (commentText) {
            const result = await createArticleComment(slug, { body: commentText });
            setComments([result.comment, ...comments]);
            setCommentText("");
        }
    };

    return (
        <div className="row">
            <div className="col-xs-12 col-md-8 offset-md-2">
                {user && (
                    <form onSubmit={handleSubmit} className="card comment-form">
                        <div className="card-block">
                            <textarea
                                className="form-control"
                                placeholder="Write a comment..."
                                rows={3}
                                value={commentText}
                                onChange={(e) => setCommentText(e.currentTarget.value)}
                            ></textarea>
                        </div>
                        <div className="card-footer">
                            <img
                                src={user.image || "/default-avatar.svg"}
                                alt="avatar"
                                className="comment-author-img"
                            />
                            <button type="submit" className="btn btn-sm btn-primary">
                                Post Comment
                            </button>
                        </div>
                    </form>
                )}

                {comments.map((comment) => (
                    <div key={comment.id} className="card">
                        <div className="card-block">
                            <p className="card-text">{comment.body}</p>
                        </div>
                        <div className="card-footer">
                            <a
                                href={`/profile/${comment.author.username}`}
                                className="comment-author"
                            >
                                <img
                                    src={comment.author.image || "/default-avatar.svg"}
                                    alt="avatar"
                                    className="comment-author-img"
                                />
                            </a>
                            &nbsp;
                            <a
                                href={`/profile/${comment.author.username}`}
                                className="comment-author"
                            >
                                {comment.author.username}
                            </a>
                            <span className="date-posted">
                                {new Date(comment.createdAt).toLocaleDateString()}
                            </span>
                            {comment.author.username === user?.username && (
                                <span
                                    className="mod-options"
                                    onClick={async () => {
                                        await deleteArticleComment(slug, comment.id);
                                        setComments(comments.filter((c) => c.id !== comment.id));
                                    }}
                                >
                                    <i className="ion-trash-a"></i>
                                </span>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
