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
	IncorrectPassword    = errors.New("provided password was incorrect")
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

func InsertExperiment(password string, openNullable sql.NullTime) (uint32, error) {
	hashed, err := utils.HashPassword(password)
	if err != nil {
		return 0, err
	}

	utils.Log("password is: %v", password)

	open := time.Now()
	if openNullable.Valid {
		open = openNullable.Time
	}

	row := psql.Insert("experiments").
		Columns("hash", "open").
		Values(hashed, open).
		Suffix("RETURNING \"id\"").
		RunWith(globalDb).
		QueryRow()

	var id uint32
	err = row.Scan(&id)
	if err != nil {
		return 0, err
	}

	return id, nil
}

func InsertUser(password string) (uint64, string, error) {
	hashed, err := utils.HashPassword(password)
	if err != nil {
		return 0, "", err
	}

	row := psql.Select("id", "began", "ended").
		From("experiments").
		Where(sq.Eq{"hash": hashed}).
		RunWith(globalDb).
		QueryRow()

	var experimentId uint32
	var began, ended sql.NullTime
	err = row.Scan(&experimentId, &began, &ended)
	if err != nil {
		return 0, "", err
	}

	now := time.Now()
	if began.Valid && now.After(began.Time) {
		return 0, "", ExperimentClosed
	} else if ended.Valid && now.After(ended.Time) {
		return 0, "", ExperimentOver
	}

	token := utils.RandomString(127)
	hashedToken, err := utils.HashPassword(token)
	if err != nil {
		return 0, "", err
	}

	id := utils.RandomLong()
	_, err = psql.Insert("devices").
		Columns("id", "experiment", "hash").
		Values(id, experimentId, hashedToken).
		RunWith(globalDb).
		Exec()
	if err != nil {
		return 0, "", err
	}

	return id, token, nil
}

func InsertConnections(connections ConnectionInfo) error {
	hashed, err := utils.HashPassword(connections.Key)
	if err != nil {
		return err
	}

	row := psql.Select("devices.id", "experiments.began", "experiments.ended").
		From("devices").
		Where(sq.Eq{"hash": hashed}).
		RunWith(globalDb).
		QueryRow()

	var deviceId int64
	var began, ended sql.NullTime
	err = row.Scan(&deviceId, &began, &ended)
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
