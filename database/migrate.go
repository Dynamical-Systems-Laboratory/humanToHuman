package database

import (
	"database/sql"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	sq "github.com/Masterminds/squirrel"
	migrate "github.com/golang-migrate/migrate/v4"
	"github.com/golang-migrate/migrate/v4/database/postgres"
	_ "github.com/golang-migrate/migrate/v4/source/pkger"
	_ "github.com/lib/pq"
	"github.com/markbates/pkger"
	"log"
	"time"
)

// version defines the current migration version. This ensures the app
// is always compatible with the version of the database.
const (
	CompatVersion = 1
	DefaultURL    = "postgres://humantohuman:humantohuman@localhost/humantohuman?sslmode=disable"
	TimeFormat    = time.RFC3339
)

var (
	globalDb      *sql.DB          = nil
	globalMigrate *migrate.Migrate = nil
	psql                           = sq.StatementBuilder.PlaceholderFormat(sq.Dollar)
)

func getMigrateInstance(db *sql.DB) *migrate.Migrate {
	driver, err := postgres.WithInstance(db, new(postgres.Config))
	utils.FailIf(err, " why did this fail?")

	pkger.Include("/migrations")
	migrateInstance, err := migrate.NewWithDatabaseInstance("pkger:///migrations", "postgres", driver)
	utils.FailIf(err, "why did this fail?")
	return migrateInstance
}

func getDb(dbURL string) (*sql.DB, *migrate.Migrate) {
	if globalDb != nil {
		return globalDb, globalMigrate
	}

	connStr := dbURL
	var err error
	globalDb, err = sql.Open("postgres", connStr)
	if err != nil {
		log.Fatal(err)
	}

	globalMigrate = getMigrateInstance(globalDb)
	return globalDb, globalMigrate
}

func ClearConnections() error {
	_, err := psql.Delete("connections").
		RunWith(globalDb).
		Exec()
	return err
}

func Clear() error {
	err := globalMigrate.Down()
	if err != nil && err != migrate.ErrNoChange {
		return err
	}

	err = globalMigrate.Drop()
	if err != nil && err != migrate.ErrNoChange {
		return err
	}
	globalMigrate = getMigrateInstance(globalDb)
	err = globalMigrate.Migrate(CompatVersion)
	if err != nil {
		return err
	}
	return nil
}

func ConnectToDb(dbURL string) {
	if globalDb != nil {
		return
	}

	log.Println("Connecting to", dbURL)

	getDb(dbURL)
	err := globalMigrate.Migrate(CompatVersion)
	if err != nil && err != migrate.ErrNoChange {
		log.Fatal("Error when migrating: ", err)
	}
}
