package configuration

import (
	_ "embed"
	"fmt"

	"github.com/go-chi/httplog/v2"
	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/jmoiron/sqlx"
	_ "modernc.org/sqlite"
)

//go:embed sqlite_ddl.sql
var sqliteSchema string

// NewDatabase creates a new SQL database connection based on the provided configuration.
// It supports both "postgres" and "sqlite" (in-memory) drivers.
func NewDatabase(cfg DatabaseConfig, logger *httplog.Logger) (*sqlx.DB, error) {
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

	// The sqlite database is empty and must be initialized
	if cfg.Type == "sqlite" {
		// Ensure only one connection is used for in-memory sqlite to maintain state, because each :memory: connection
		// is a separate database. This means requests could fail as they query an empty database.
		db.SetMaxOpenConns(1)

		logger.Info("Initializing SQLite schema")
		if _, err := db.Exec(sqliteSchema); err != nil {
			return nil, fmt.Errorf("failed to initialize sqlite schema: %w", err)
		}
	} else {
		// Limit connections for Postgres to prevent "too many clients" errors
		db.SetMaxOpenConns(10)
		db.SetMaxIdleConns(10)
		logger.Info("Setting Postgres connection limits", "maxOpen", 10, "maxIdle", 10)
	}

	logger.Info("Successfully connected to database")
	return db, nil
}
