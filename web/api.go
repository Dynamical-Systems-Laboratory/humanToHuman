package web

import (
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	"github.com/gin-gonic/gin"
	"time"
)

type OneWayConnection struct {
	Time  string  `json:"time"`
	Other int64   `json:"other"`
	Power int64   `json:"power"`
	Rssi  float64 `json:"rssi"`
}

type ConnectionInfo struct {
	Id          int64              `json:"id"`
	Connections []OneWayConnection `json:"connections"`
}

type ErrorApiMessage struct {
	Status  uint64 `json:"status"`
	Message string `json:"message"`
}

type OkApiMessage struct {
	Status uint64      `json:"status"`
	Value  interface{} `json:"value"`
}

var (
	MissingLogin       = errors.New("missing login query parameter")
	MissingPassword    = errors.New("missing password query parameter")
	NoLoginInformation = errors.New("neither login nor password was provided")
	MissingToken       = errors.New("missing token")
	TooManyAuthMethods = errors.New("gave too many authorization methods")
)

func JsonInfer(c *gin.Context, object interface{}, err error) {
	if err != nil {
		utils.IError(err, "Error on path %v", c.Request.URL)
		c.JSON(400, ErrorApiMessage{400, err.Error()})
	} else {
		c.JSON(200, object)
	}
}

func JsonFail(c *gin.Context, err error) bool {
	if err != nil {
		utils.IError(err, "Error on path %v", c.Request.URL)
		c.JSON(400, ErrorApiMessage{400, err.Error()})
		return true
	}
	return false
}

// AddUser godoc
// @Summary Adds a user
// @Success 200 {object} uint64
// @Failure 400 {object} web.ErrorApiMessage
// @Router /addUser [post]
func NewUser(c *gin.Context) {
	id, err := database.InsertUser(c.PostForm("token"))
	JsonInfer(c, id, err)
}

// AddConnections godoc
// @Summary Adds a set of connection
// @Param id formData string true "id of current device"
// @Param time formData string true "time of connection: 2012-11-01T22:08:41+00:00"
// @Param other formData uint64 true "device connected to"
// @Param power formData int32 true "power of the connection"
// @Param rssi formData float64 true "rssi of the connection"
// @Success 200 {object} uint64
// @Failure 400 {object} web.ErrorApiMessage
// @Router /addConnections [post]
func AddConnections(c *gin.Context) {
	var userConns ConnectionInfo
	err := c.BindJSON(&userConns)
	if JsonFail(c, err) {
		return
	}

	var conn database.Connection
	conn.DeviceA = userConns.Id
	connections := make([]database.Connection, 100)[:0]
	for _, connection := range userConns.Connections {
		conn.Time, err = time.Parse(database.TimeFormat, connection.Time)
		if JsonFail(c, err) {
			return
		}

		conn.DeviceB = connection.Other
		conn.Power = connection.Power
		conn.Rssi = connection.Rssi
		connections = append(connections, conn)
	}

	err = database.InsertConnectionsUnsafe(connections)
	JsonInfer(c, len(connections), err)
}
