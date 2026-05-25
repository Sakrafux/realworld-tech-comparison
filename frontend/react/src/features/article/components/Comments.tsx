import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { type SubmitEvent, useState } from "react";
import defaultAvatar from "@/shared/assets/default-avatar.svg";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { getUser } from "@/shared/api/features/user-api.ts";
import {
    createArticleComment,
    deleteArticleComment,
    getArticleComments,
    type MultipleCommentsResponse,
} from "@/shared/api/features/comment-api.ts";
import { Link } from "@tanstack/react-router";

export type CommentsProps = {
    slug: string;
    isAuthor: boolean;
};

export default function Comments({ slug, isAuthor }: CommentsProps) {
    const [commentText, setCommentText] = useState<string>();

    const { username, isAuthenticated } = useAuth();
    const queryClient = useQueryClient();

    const { data: user } = useQuery({
        queryKey: ["user"],
        queryFn: () => getUser(),
        enabled: isAuthenticated,
    });

    const { data: comments } = useQuery({
        queryKey: ["comments"],
        queryFn: () => getArticleComments(slug),
    });

    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (commentText) {
            const result = await createArticleComment(slug, { body: commentText });
            queryClient.setQueryData(["comments"], (oldData: MultipleCommentsResponse) => ({
                comments: [result.comment, ...oldData.comments],
            }));

            setCommentText("");
        }
    };

    return (
        <div className="row">
            <div className="col-xs-12 col-md-8 offset-md-2">
                {isAuthenticated && user && !isAuthor && (
                    <form onSubmit={handleSubmit} className="card comment-form">
                        <div className="card-block">
                            <textarea
                                className="form-control"
                                placeholder="Write a comment..."
                                rows={3}
                                value={commentText}
                                onChange={(e) => setCommentText(e.target.value)}
                            ></textarea>
                        </div>
                        <div className="card-footer">
                            <img
                                src={user.user.image || defaultAvatar}
                                alt="avatar"
                                className="comment-author-img"
                            />
                            <button type="submit" className="btn btn-sm btn-primary">
                                Post Comment
                            </button>
                        </div>
                    </form>
                )}

                {comments &&
                    comments.comments.map((comment) => (
                        <div key={comment.id} className="card">
                            <div className="card-block">
                                <p className="card-text">{comment.body}</p>
                            </div>
                            <div className="card-footer">
                                <Link
                                    to={`/profile/${comment.author.username}`}
                                    className="comment-author"
                                >
                                    <img
                                        src={comment.author.image || defaultAvatar}
                                        alt="avatar"
                                        className="comment-author-img"
                                    />
                                </Link>
                                &nbsp;
                                <Link
                                    to={`/profile/${comment.author.username}`}
                                    className="comment-author"
                                >
                                    {comment.author.username}
                                </Link>
                                <span className="date-posted">
                                    {new Date(comment.createdAt).toLocaleDateString()}
                                </span>
                                {comment.author.username === username && (
                                    <span
                                        className="mod-options"
                                        onClick={async () => {
                                            await deleteArticleComment(slug, comment.id);
                                            queryClient.setQueryData(
                                                ["comments"],
                                                (oldData: MultipleCommentsResponse) => ({
                                                    comments: oldData.comments.filter(
                                                        (oldComment) =>
                                                            oldComment.id !== comment.id,
                                                    ),
                                                }),
                                            );
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
