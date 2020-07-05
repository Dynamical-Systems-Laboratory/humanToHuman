package database

import (
	"database/sql"
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	sq "github.com/Masterminds/squirrel"
	"time"
)

var (
	NewUserFailed        = errors.New("failed to make new user")
	AuthFailed           = errors.New("failed to authenticate")
	IncorrectPassword    = errors.New("provided password was incorrect")
	ExperimentOver       = errors.New("experiment has already completed")
	ExperimentClosed     = errors.New("experiment has already closed")
	ExperimentNotStarted = errors.New("experiment hasn't started yet")
)

func GetDataForExperiment(password string) ([]Connection, error) {
	hashed, err := utils.HashPassword(password)
	if err != nil {
		return nil, err
	}

	rows, err := psql.Select("devices.id").
		From("devices").
		Join("experiments ON experiments.id = devices.experiment").
		Where(sq.Eq{"experiments.hash": hashed}).
		RunWith(globalDb).
		Query()
	if err != nil {
		rows.Close()
		return nil, err
	}

	users := make([]int64, 50)[:0]
	var user int64
	for rows.Next() {
		err := rows.Scan(&user)
		if err != nil {
			return nil, err
		}

		users = append(users, user)
	}

	if err = rows.Close(); err != nil {
		return nil, err
	}

	rows, err = psql.Select("time", "device_a", "device_b",
		"measured_power", "rssi").
		From("connections").
		Where(sq.Or{sq.Eq{"device_a": users}, sq.Eq{"device_b": users}}).
		RunWith(globalDb).
		Query()
	if err != nil {
		rows.Close()
		return nil, err
	}

	connections := make([]Connection, 1000)[:0]
	var connection Connection
	for rows.Next() {
		err = rows.Scan(&connection.Time, &connection.Scanner, &connection.Advertiser,
			&connection.Power, &connection.Rssi)
		if err != nil {
			return nil, err
		}

		connections = append(connections, connection)
	}

	if err = rows.Close(); err != nil {
		return nil, err
	}

	return connections, nil
}

func GetDescription(password string) (string, error) {
	hashed, err := utils.HashPassword(password)
	if err != nil {
		return "", err
	}

	row := psql.Select("description").
		From("experiments").
		Where(sq.Eq{"hash": hashed}).
		RunWith(globalDb).
		QueryRow()

	var description string
	err = row.Scan(&description)
	return description, err
}

func GetPrivacyPolicy(password string) (string, error) {
	hashed, err := utils.HashPassword(password)
	if err != nil {
		return "", err
	}

	row := psql.Select("policy").
		From("experiments").
		Where(sq.Eq{"hash": hashed}).
		RunWith(globalDb).
		QueryRow()

	var policy string
	err = row.Scan(&policy)
	return policy, err
}

func InsertExperiment(password, privacyPolicy, description string,
	openNullable sql.NullTime) (uint32, error) {
	hashed, err := utils.HashPassword(password)
	if err != nil {
		return 0, err
	}

	open := time.Now()
	if openNullable.Valid {
		open = openNullable.Time
	}

	row := psql.Insert("experiments").
		Columns("hash", "policy", "description", "open").
		Values(hashed, privacyPolicy, description, open).
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
		builder = builder.Values(time.Time(connection.Time), deviceId,
			connection.Other, connection.Power, connection.Rssi)
	}

	_, err = builder.Exec()
	return err
}

func InsertConnectionsUnsafe(connections ConnectionInfoUnsafe) error {
	builder := psql.Insert("connections").
		Columns("time", "device_a", "device_b", "measured_power", "rssi").
		RunWith(globalDb)

	for _, connection := range connections.Connections {
		builder = builder.Values(time.Time(connection.Time), connections.Id,
			connection.Other, connection.Power, connection.Rssi)
	}

	_, err := builder.Exec()
	return err
}
