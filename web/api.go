package web

import (
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	"github.com/gin-gonic/gin"
)

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
func AddConnectionsUnsafe(c *gin.Context) {
	var connections database.ConnectionInfoUnsafe
	err := c.BindJSON(&connections)
	if JsonFail(c, err) {
		return
	}

	err = database.InsertConnectionsUnsafe(connections)
	JsonInfer(c, len(connections.Connections), err)
}

func AddConnections(c *gin.Context) {
	var userConns database.ConnectionInfo
	err := c.BindJSON(&userConns)
	if JsonFail(c, err) {
		return
	}

	err = database.InsertConnections(userConns)
	JsonInfer(c, len(userConns.Connections), err)
}
