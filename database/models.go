package database

import (
	"time"
)

type Connection struct {
	Time    time.Time
	DeviceA int64
	DeviceB int64
	Power   int64
	Rssi    float64
}
