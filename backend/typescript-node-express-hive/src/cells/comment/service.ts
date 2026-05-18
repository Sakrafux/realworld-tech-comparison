import { Comment } from "./comment.js";
import type {
    CommentService,
    CommentRepository,
    UserProvider,
    ArticleProvider,
    CreateCommentCommand,
    GetCommentsQuery,
    DeleteCommentCommand,
} from "./ports.js";
import { newForbiddenError, newResourceNotFound } from "../../shared/errors/app-error.js";

export class DefaultCommentService implements CommentService {
    constructor(
        private repo: CommentRepository,
        private userProvider: UserProvider,
        private articleProvider: ArticleProvider,
    ) {}

    async createComment(cmd: CreateCommentCommand): Promise<Comment> {
        const article = await this.articleProvider.getArticle({ slug: cmd.slug });
        const author = await this.userProvider.getUser(cmd.authorId);

        const comment = new Comment(
            0,
            new Date(),
            new Date(),
            cmd.body,
            {
                username: author.username,
                bio: author.bio,
                image: author.image,
                following: false,
            },
        );

        await this.repo.create(comment, article.id, cmd.authorId);
        return comment;
    }

    async getComments(query: GetCommentsQuery): Promise<Comment[]> {
        const article = await this.articleProvider.getArticle({ slug: query.slug });
        return this.repo.findByArticleId(article.id, query.observerId);
    }

    async deleteComment(cmd: DeleteCommentCommand): Promise<void> {
        // We don't strictly need the articleId here if we trust the commentId, 
        // but RealWorld API usually includes the slug in the URL.
        const result = await this.repo.getById(cmd.commentId);
        if (!result) {
            throw newResourceNotFound("Comment", "id", cmd.commentId);
        }

        if (result.authorId !== cmd.userId) {
            throw newForbiddenError("You are not the author of this comment");
        }

        await this.repo.delete(cmd.commentId);
    }
}
