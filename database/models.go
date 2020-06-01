package database

import (
	"time"
)

type Connection struct {
	Time    time.Time
	DeviceA uint64
	DeviceB uint64
	Power   uint64
	Rssi    float64
}
