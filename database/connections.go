package database

import (
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
)

var NewUserFailed = errors.New("failed to make new user")

func InsertUser() (uint64, error) {
	for i := 0; i < 10; i++ {
		id := utils.RandomLong()
		_, err := psql.Insert("devices").
			Columns("id").
			Values(id).
			RunWith(globalDb).
			Exec()
		if err == nil {
			return id, nil
		}
	}

	return 0, NewUserFailed
}

func InsertConnections(connections []Connection) error {
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
