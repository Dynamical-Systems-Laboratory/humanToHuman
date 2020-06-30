package web

import (
	"database/sql"
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	"github.com/gin-gonic/gin"
	"strconv"
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

func ParamUint(c *gin.Context, param string) (uint32, error) {
	valString := c.Param(param)
	val, err := strconv.ParseUint(valString, 10, 64)
	return uint32(val), err
}

func GetPrivacyPolicy(c *gin.Context) {
	policy, err := database.GetPrivacyPolicy()
	JsonInfer(c, policy, err)
}

func NewExperimentBrowser(c *gin.Context) {
	password, ok := c.GetQuery("password")
	if !ok {
		password = utils.RandomString(127)
	}

	utils.Log("password is: %v", password)

	type Response struct {
		Password string `json:"password"`
		Id       uint32 `json:"id"`
	}

	id, err := database.InsertExperiment(password, sql.NullTime{})
	JsonInfer(c, Response{password, id}, err)
}

func NewExperiment(c *gin.Context) {
	password, ok := c.GetPostForm("password")
	if !ok {
		password = utils.RandomString(127)
	}

	type Response struct {
		Password string `json:"password"`
		Id       uint32 `json:"id"`
	}

	id, err := database.InsertExperiment(password, sql.NullTime{})
	JsonInfer(c, Response{password, id}, err)
}

func NewUser(c *gin.Context) {
	id, token, err := database.InsertUser(c.Param("experiment"))
	type Response struct {
		Id    uint64 `json:"id"`
		Token string `json:"token"`
	}
	JsonInfer(c, Response{id, token}, err)
}

func AddConnectionsUnsafe(c *gin.Context) {
	var connections database.ConnectionInfoUnsafe
	err := c.BindJSON(&connections)
	if JsonFail(c, err) {
		return
	}

	utils.Log("connection values are: %v", connections)
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
