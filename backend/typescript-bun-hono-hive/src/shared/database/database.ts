import pgPromise from "pg-promise";
import type { DatabaseConfig } from "../config/config.js";

const pgp = pgPromise();

export function newDatabase(cfg: DatabaseConfig) {
    const connectionString = `postgres://${cfg.user}:${cfg.password}@${cfg.host}:${cfg.port}/${cfg.name}?sslmode=${cfg.sslMode}`;

    console.log(`Connecting to Postgres database: ${cfg.host}:${cfg.port}/${cfg.name}`);

    return pgp(connectionString);
}

export type Database = pgPromise.IDatabase<any>;
