package database

import (
	"database/sql"
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	sq "github.com/Masterminds/squirrel"
	"time"
)

const (
	PRIVACY_POLICY int32 = 1
)

var (
	NewUserFailed        = errors.New("failed to make new user")
	AuthFailed           = errors.New("failed to authenticate")
	ExperimentOver       = errors.New("experiment has already completed")
	ExperimentClosed     = errors.New("experiment has already closed")
	ExperimentNotStarted = errors.New("experiment hasn't started yet")
)

func GetPrivacyPolicy() (string, error) {
	row := psql.Select("tdata").
		From("metadata").
		Where(sq.Eq{"id": PRIVACY_POLICY}).
		RunWith(globalDb).
		QueryRow()

	var policy string
	err := row.Scan(&policy)
	return policy, err
}

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

	now := time.Now()
	if began.Valid && now.After(began.Time) {
		return 0, ExperimentClosed
	} else if ended.Valid && now.After(ended.Time) {
		return 0, ExperimentOver
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

func InsertConnections(connections ConnectionInfo) error {
	row := psql.Select("devices.id", "experiments.began", "experiments.ended").
		From("devices").
		Join("experiments ON devices.experiment = experiments.id").
		Where(sq.Eq{"token": connections.Key}).
		RunWith(globalDb).
		QueryRow()

	var deviceId int64
	var began, ended sql.NullTime
	err := row.Scan(&deviceId, &began, &ended)
	if err != nil {
		return err
	}

	now := time.Now()
	if !began.Valid || now.Before(began.Time) {
		return ExperimentNotStarted
	} else if ended.Valid && now.After(ended.Time) {
		return ExperimentOver
	}

	builder := psql.Insert("connections").
		Columns("time", "device_a", "device_b", "measured_power", "rssi").
		RunWith(globalDb)
	for _, connection := range connections.Connections {
		builder = builder.Values(time.Time(connection.Time), deviceId, connection.Other,
			connection.Power, connection.Rssi)
	}

	_, err = builder.Exec()
	return err
}

func InsertConnectionsUnsafe(connections ConnectionInfoUnsafe) error {
	builder := psql.Insert("connections").
		Columns("time", "device_a", "device_b", "measured_power", "rssi").
		RunWith(globalDb)

	for _, connection := range connections.Connections {
		builder = builder.Values(time.Time(connection.Time), connections.Id, connection.Other,
			connection.Power, connection.Rssi)
	}

	_, err := builder.Exec()
	return err
}
