export interface ServerConfig {
    port: string;
}

export interface DatabaseConfig {
    type: string;
    host: string;
    port: string;
    user: string;
    password: string;
    name: string;
    sslMode: string;
}

export interface WebConfig {
    corsAllowedOrigins: string[];
}

export interface SecurityConfig {
    jwtSecret: string;
}

export interface Config {
    server: ServerConfig;
    database: DatabaseConfig;
    web: WebConfig;
    security: SecurityConfig;
}

export function loadConfig(): Config {
    return {
        server: {
            port: getEnv("SERVER_PORT", "8080"),
        },
        database: {
            type: getEnv("DB_TYPE", "sqlite"),
            host: getEnv("DB_HOST", "localhost"),
            port: getEnv("DB_PORT", "5432"),
            user: getEnv("DB_USER", "postgres"),
            password: getEnv("DB_PASSWORD", "password"),
            name: getEnv("DB_NAME", "realworld"),
            sslMode: getEnv("DB_SSLMODE", "disable"),
        },
        web: {
            corsAllowedOrigins: getEnvArray("CORS_ALLOWED_ORIGINS", ["*"]),
        },
        security: {
            jwtSecret: getEnv("JWT_SECRET", "super-secret-key"),
        },
    };
}

function getEnv(key: string, fallback: string): string {
    return process.env[key] ?? fallback;
}

function getEnvArray(key: string, fallback: string[]): string[] {
    const value = process.env[key];
    if (value) {
        return value.split(",");
    }
    return fallback;
}
