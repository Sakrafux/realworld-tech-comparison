import type { Comment } from "./comment.domain.js";

export interface CreateCommentCommand {
    slug: string;
    authorId: number;
    body: string;
}

export interface DeleteCommentCommand {
    slug: string;
    commentId: number;
    userId: number;
}

export interface GetCommentsQuery {
    slug: string;
    observerId?: number;
}

export interface CommentService {
    createComment(cmd: CreateCommentCommand): Promise<Comment>;
    getComments(query: GetCommentsQuery): Promise<Comment[]>;
    deleteComment(cmd: DeleteCommentCommand): Promise<void>;
}

export interface CommentRepository {
    create(comment: Comment, articleId: number, authorId: number): Promise<void>;
    findByArticleId(articleId: number, observerId?: number): Promise<Comment[]>;
    getById(id: number): Promise<{ comment: Comment; articleId: number; authorId: number } | null>;
    delete(id: number): Promise<void>;
}

export interface UserInfo {
    id: number;
    username: string;
    bio: string;
    image: string | null;
}

export interface ArticleInfo {
    id: number;
    slug: string;
}

export interface UserProvider {
    getUser(id: number): Promise<UserInfo>;
}

export interface ArticleProvider {
    getArticle(query: { slug: string }): Promise<{ id: number }>;
}
