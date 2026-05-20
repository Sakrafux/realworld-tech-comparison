import { Hono } from "hono";
import { type Config, loadConfig } from "../shared/config/config.js";
import { newDatabase, type Database } from "../shared/database/database.js";
import { errorHandler } from "../shared/web/error-handler.js";
import { BunPasswordHasher } from "../shared/security/password.js";
import { JwtTokenGenerator } from "../shared/security/token.js";

// User Cell
import { PostgresUserRepository } from "../cells/user/user.repository.js";
import { DefaultUserService } from "../cells/user/user.service.js";
import { UserHandler } from "../cells/user/user.controller.js";

// Article Cell
import { PostgresArticleRepository } from "../cells/article/article.repository.js";
import { DefaultArticleService } from "../cells/article/article.service.js";
import { ArticleHandler } from "../cells/article/article.controller.js";

// Comment Cell
import { PostgresCommentRepository } from "../cells/comment/comment.repository.js";
import { DefaultCommentService } from "../cells/comment/comment.service.js";
import { CommentHandler } from "../cells/comment/comment.controller.js";

type Variables = {
    userId?: number;
};

export class App {
    private readonly honoApp: Hono<{ Variables: Variables }>;
    private readonly config: Config;
    private readonly db: Database;

    constructor(config?: Config) {
        this.honoApp = new Hono<{ Variables: Variables }>();
        this.config = config ?? loadConfig();
        this.db = newDatabase(this.config.database);
    }

    getDatabase(): Database {
        return this.db;
    }

    async bootstrap(): Promise<Hono<{ Variables: Variables }>> {
        // Health Check
        this.honoApp.get("/health", (c) => {
            return c.text("OK", 200);
        });

        // Shared Components
        const passwordHasher = new BunPasswordHasher();
        const jwtTokenGenerator = new JwtTokenGenerator(this.config.security.jwtSecret);

        // User Cell wiring
        const userRepo = new PostgresUserRepository(this.db);
        const userService = new DefaultUserService(userRepo, passwordHasher);
        const userHandler = new UserHandler(userService, jwtTokenGenerator, jwtTokenGenerator);

        // Article Cell wiring
        const articleRepo = new PostgresArticleRepository(this.db);
        const articleService = new DefaultArticleService(articleRepo, userService);
        const articleHandler = new ArticleHandler(articleService, jwtTokenGenerator);

        // Comment Cell wiring
        const commentRepo = new PostgresCommentRepository(this.db);
        const commentService = new DefaultCommentService(commentRepo, userService, articleService);
        const commentHandler = new CommentHandler(commentService, jwtTokenGenerator);

        // Routing
        this.honoApp.route("/api", userHandler.getApp());
        this.honoApp.route("/api", articleHandler.getApp());
        this.honoApp.route("/api", commentHandler.getApp());

        // Error Handling
        this.honoApp.onError(errorHandler);

        return this.honoApp;
    }

    async shutdown() {
        this.db.$config.pgp.end();
    }
}
