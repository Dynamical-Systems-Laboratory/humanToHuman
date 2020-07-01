package database

import (
	"encoding/json"
	"time"
)

type Time time.Time

func (t *Time) UnmarshalJSON(j []byte) error {
	var s string
	err := json.Unmarshal(j, &s)
	if err != nil {
		return err
	}

	time, err := time.Parse(TimeFormat, s)
	if err != nil {
		return err
	}

	*t = Time(time)
	return nil
}

type OneWayConnection struct {
	Time  Time  `json:"time"`
	Other int64 `json:"other"`
	Power int16 `json:"power"`
	Rssi  int16 `json:"rssi"`
}

type ConnectionInfo struct {
	Key         string             `json:"key"`
	Connections []OneWayConnection `json:"connections"`
}

type ConnectionInfoUnsafe struct {
	Id          int64              `json:"id"`
	Connections []OneWayConnection `json:"connections"`
}
