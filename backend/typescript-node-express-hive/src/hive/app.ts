import express, { type Express } from "express";
import { type Config, loadConfig } from "../shared/config/config.js";
import { newDatabase, type Database } from "../shared/database/database.js";
import { errorHandler } from "../shared/web/error-handler.js";
import { Argon2PasswordHasher } from "../shared/security/password.js";
import { JwtTokenGenerator } from "../shared/security/token.js";

// User Cell
import { PostgresUserRepository } from "../cells/user/repository.js";
import { DefaultUserService } from "../cells/user/service.js";
import { UserHandler } from "../cells/user/handler.js";

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

        // Shared Components
        const passwordHasher = new Argon2PasswordHasher();
        const jwtTokenGenerator = new JwtTokenGenerator(this.config.security.jwtSecret);

        // User Cell wiring
        const userRepo = new PostgresUserRepository(this.db);
        const userService = new DefaultUserService(userRepo, passwordHasher);
        const userHandler = new UserHandler(userService, jwtTokenGenerator, jwtTokenGenerator);

        // Routing
        this.expressApp.use("/api", userHandler.getRouter());

        // Error Handling
        this.expressApp.use(errorHandler);

        return this.expressApp;
    }

    async shutdown() {
        this.db.$config.pgp.end();
    }
}
