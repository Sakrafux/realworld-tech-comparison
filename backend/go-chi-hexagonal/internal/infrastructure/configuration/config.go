package configuration

import (
	"os"
	"strings"
)

type ServerConfig struct {
	Port string
}

type DatabaseConfig struct {
	// Type is 'sqlite' or 'postgres'
	Type     string
	Host     string
	Port     string
	User     string
	Password string
	Name     string
	SSLMode  string
}

type WebConfig struct {
	CorsAllowedOrigins []string
}

type SecurityConfig struct {
	JWTSecret string
}

type OtelConfig struct {
	Enabled     bool
	Endpoint    string
	ServiceName string
}

// Config is the root configuration object for the application.
type Config struct {
	Server   ServerConfig
	Database DatabaseConfig
	Web      WebConfig
	Security SecurityConfig
	Otel     OtelConfig
}

// LoadConfig reads application configuration from environment variables with sensible defaults.
func LoadConfig() *Config {
	return &Config{
		Server: ServerConfig{
			Port: getEnv("SERVER_PORT", "8080"),
		},
		Database: DatabaseConfig{
			Type:     getEnv("DB_TYPE", "sqlite"),
			Host:     getEnv("DB_HOST", "localhost"),
			Port:     getEnv("DB_PORT", "5432"),
			User:     getEnv("DB_USER", "postgres"),
			Password: getEnv("DB_PASSWORD", "password"),
			Name:     getEnv("DB_NAME", "realworld"),
			SSLMode:  getEnv("DB_SSLMODE", "disable"),
		},
		Web: WebConfig{
			CorsAllowedOrigins: getEnvArray("CORS_ALLOWED_ORIGINS", []string{"*"}),
		},
		Security: SecurityConfig{
			JWTSecret: getEnv("JWT_SECRET", "super-secret-key"),
		},
		Otel: OtelConfig{
			Enabled:     getEnvBool("OTEL_ENABLED", "false"),
			Endpoint:    getEnv("OTEL_EXPORTER_OTLP_ENDPOINT", "localhost:4318"),
			ServiceName: getEnv("OTEL_SERVICE_NAME", "realworld"),
		},
	}
}

func getEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func getEnvBool(key, fallback string) bool {
	value := getEnv(key, fallback)
	return value == "true"
}

func getEnvArray(key string, fallback []string) []string {
	if value, ok := os.LookupEnv(key); ok {
		return strings.Split(value, ",")
	}
	return fallback
}
