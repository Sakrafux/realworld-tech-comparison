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

export interface OtelConfig {
    enabled: boolean;
    endpoint: string;
    serviceName: string;
    diagnosticLoggingEnabled: boolean;
}

export interface Config {
    server: ServerConfig;
    database: DatabaseConfig;
    web: WebConfig;
    security: SecurityConfig;
    otel: OtelConfig;
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
        otel: {
            enabled: getEnvBool("OTEL_ENABLED", "false"),
            endpoint: getEnv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4318"),
            serviceName: getEnv("OTEL_SERVICE_NAME", "realworld"),
            diagnosticLoggingEnabled: getEnvBool("OTEL_DIAGNOSTIC_LOGGING_ENABLED", "false"),
        },
    };
}

function getEnv(key: string, fallback: string): string {
    return process.env[key] ?? fallback;
}

function getEnvBool(key: string, fallback: string): boolean {
    const value = getEnv(key, fallback);
    return value === "true";
}

function getEnvArray(key: string, fallback: string[]): string[] {
    const value = process.env[key];
    if (value) {
        return value.split(",");
    }
    return fallback;
}
