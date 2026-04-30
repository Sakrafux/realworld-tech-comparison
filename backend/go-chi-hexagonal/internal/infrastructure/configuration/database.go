package configuration

import (
	_ "embed"
	"fmt"
	"log/slog"

	"github.com/jmoiron/sqlx"
	_ "github.com/lib/pq"
	_ "modernc.org/sqlite"
)

//go:embed sqlite_ddl.sql
var sqliteSchema string

func NewDatabase(cfg DatabaseConfig) (*sqlx.DB, error) {
	var driver, dsn string

	if cfg.Type == "sqlite" {
		driver = "sqlite"
		dsn = ":memory:"
		slog.Info("Using in-memory SQLite database")
	} else {
		driver = "postgres"
		dsn = fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
			cfg.Host, cfg.Port, cfg.User, cfg.Password, cfg.Name, cfg.SSLMode)
		slog.Info("Connecting to Postgres database", "host", cfg.Host, "port", cfg.Port, "dbname", cfg.Name)
	}

	db, err := sqlx.Connect(driver, dsn)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to database: %w", err)
	}

	if cfg.Type == "sqlite" {
		slog.Info("Initializing SQLite schema")
		if _, err := db.Exec(sqliteSchema); err != nil {
			return nil, fmt.Errorf("failed to initialize sqlite schema: %w", err)
		}
	}

	slog.Info("Successfully connected to database")
	return db, nil
}
