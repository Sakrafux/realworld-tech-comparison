import express, { type Express } from "express";
import { type Config, loadConfig } from "../shared/config/config.js";
import { newDatabase, type Database } from "../shared/database/database.js";
import { errorHandler } from "../shared/web/error-handler.js";
import { Argon2PasswordHasher } from "../shared/security/password.js";
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

export class App {
    private readonly expressApp: Express;
    private readonly config: Config;
    private readonly db: Database;

    constructor(config?: Config) {
        this.expressApp = express();
        this.config = config ?? loadConfig();
        this.db = newDatabase(this.config.database);
    }

    getDatabase(): Database {
        return this.db;
    }

    async bootstrap(): Promise<Express> {
        this.expressApp.use(express.json());

        // Health Check
        this.expressApp.get("/health", (req, res) => {
            res.status(200).send("OK");
        });

        // Shared Components
        const passwordHasher = new Argon2PasswordHasher();
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
        this.expressApp.use("/api", userHandler.getRouter());
        this.expressApp.use("/api", articleHandler.getRouter());
        this.expressApp.use("/api", commentHandler.getRouter());

        // Error Handling
        this.expressApp.use(errorHandler);

        return this.expressApp;
    }

    async shutdown() {
        this.db.$config.pgp.end();
    }
}
