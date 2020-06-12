package database

import (
	"database/sql"
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	sq "github.com/Masterminds/squirrel"
)

var (
	NewUserFailed    = errors.New("failed to make new user")
	AuthFailed       = errors.New("failed to authenticate")
	ExperimentOver   = errors.New("experiment has already completed")
	ExperimentClosed = errors.New("experiment has already closed")
)

func InsertExperiment() (uint32, error) {
	token := utils.RandomString(127)
	row := psql.Insert("devices").
		Columns("token").
		Values(token).
		RunWith(globalDb).
		Suffix("RETURNING \"id\"").
		QueryRow()

	var id uint32
	err := row.Scan(&id)
	if err != nil {
		return 0, err
	}

	return id, nil
}

func InsertUser(experimentToken string) (uint64, error) {
	row := psql.Select("id", "began", "ended").
		From("experiments").
		Where(sq.Eq{"token": experimentToken}).
		QueryRow()

	var experiment uint32
	var began, ended sql.NullTime
	err := row.Scan(&experiment, &began, &ended)
	if err != nil {
		return 0, err
	}

	if ended.Valid {
		return 0, ExperimentOver
	} else if began.Valid {
		return 0, ExperimentClosed
	}

	token := utils.RandomString(127)
	id := utils.RandomLong()
	_, err = psql.Insert("devices").
		Columns("id", "experiment", "token").
		Values(id, experiment, token).
		RunWith(globalDb).
		Exec()
	if err == nil {
		return 0, err
	}

	return id, nil
}

func InsertConnectionsUnsafe(connections []Connection) error {
	builder := psql.Insert("connections").
		Columns("time", "device_a", "device_b", "measured_power", "rssi").
		RunWith(globalDb)

	for _, connection := range connections {
		builder = builder.Values(connection.Time, connection.DeviceA, connection.DeviceB,
			connection.Power, connection.Rssi)
	}
	_, err := builder.Exec()

	return err
}
