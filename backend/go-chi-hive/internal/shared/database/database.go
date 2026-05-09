package database

import (
	_ "embed"
	"fmt"

	"github.com/go-chi/httplog/v2"
	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/jmoiron/sqlx"
	"github.com/sakrafux/realworld-tech-comparison/backend/go-chi-hive/internal/shared/config"
	_ "modernc.org/sqlite"
)

//go:embed sqlite_ddl.sql
var sqliteSchema string

func NewDatabase(cfg config.DatabaseConfig, logger *httplog.Logger) (*sqlx.DB, error) {
	var driver, dsn string

	if cfg.Type == "sqlite" {
		driver = "sqlite"
		dsn = ":memory:"
		logger.Info("Using in-memory SQLite database")
	} else {
		driver = "pgx"
		dsn = fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
			cfg.Host, cfg.Port, cfg.User, cfg.Password, cfg.Name, cfg.SSLMode)
		logger.Info("Connecting to Postgres database (pgx)", "host", cfg.Host, "port", cfg.Port, "dbname", cfg.Name)
	}

	db, err := sqlx.Connect(driver, dsn)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to database: %w", err)
	}

	if cfg.Type == "sqlite" {
		db.SetMaxOpenConns(1)

		logger.Info("Initializing SQLite schema")
		if _, err := db.Exec(sqliteSchema); err != nil {
			return nil, fmt.Errorf("failed to initialize sqlite schema: %w", err)
		}
	} else {
		db.SetMaxOpenConns(10)
		db.SetMaxIdleConns(10)
		logger.Info("Setting Postgres connection limits", "maxOpen", 10, "maxIdle", 10)
	}

	logger.Info("Successfully connected to database")
	return db, nil
}
