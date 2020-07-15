package web

import (
	"database/sql"
	"encoding/csv"
	"errors"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	"github.com/gin-gonic/gin"
	"strconv"
	"time"
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
	Release            = false
	PasswordHash       = ""
	MissingLogin       = errors.New("missing login query parameter")
	IncorrectPassword  = errors.New("incorrect password")
	MissingPassword    = errors.New("missing password query parameter")
	NoLoginInformation = errors.New("neither login nor password was provided")
	MissingToken       = errors.New("missing token")
	TooManyAuthMethods = errors.New("gave too many authorization methods")
)

func StringInfer(c *gin.Context, value string, err error) {
	if err != nil {
		utils.IError(err, "Error on path %v", c.Request.URL)
		c.JSON(400, ErrorApiMessage{400, err.Error()})
	} else {
		c.String(200, value)
	}
}

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

func Login(c *gin.Context) {
	if Release {
		hash, err := utils.HashPassword(c.Query("password"))
		if JsonFail(c, err) {
			return
		}
		if hash != PasswordHash {
			JsonFail(c, IncorrectPassword)
			return
		}
	}

	JsonInfer(c, nil, nil)
}

func RemoveUser(c *gin.Context) {
	JsonInfer(c, nil, database.RemoveUser(c.PostForm("token")))
}

func ClearBrowser(c *gin.Context) {
	if Release {
		hash, err := utils.HashPassword(c.Query("password"))
		if JsonFail(c, err) {
			return
		}
		if hash != PasswordHash {
			JsonFail(c, IncorrectPassword)
			return
		}
	}

	if c.Query("full") == "true" {
		JsonInfer(c, nil, database.Clear())
	} else {
		JsonInfer(c, nil, database.ClearConnections())
	}
}

func Clear(c *gin.Context) {
	if Release {
		hash, err := utils.HashPassword(c.PostForm("password"))
		if JsonFail(c, err) {
			return
		}
		if hash != PasswordHash {
			JsonFail(c, IncorrectPassword)
			return
		}
	}

	if c.PostForm("full") == "true" {
		JsonInfer(c, nil, database.Clear())
	} else {
		JsonInfer(c, nil, database.ClearConnections())
	}
}

func GetCSV(c *gin.Context) {
	if Release {
		password, ok := c.GetQuery("password")
		if !ok {
			JsonFail(c, MissingPassword)
			return
		}

		hashed, err := utils.HashPassword(password)
		if JsonFail(c, err) {
			return
		}

		if hashed != PasswordHash {
			JsonFail(c, IncorrectPassword)
			return
		}
	}

	connections, err := database.GetDataForExperiment(c.Param("experiment"))
	if JsonFail(c, err) {
		return
	}

	writer := csv.NewWriter(c.Writer)
	err = writer.Write([]string{"time", "scanner", "advertiser", "power", "rssi"})
	if JsonFail(c, err) {
		return
	}
	for _, conn := range connections {
		err = writer.Write([]string{
			time.Time(conn.Time).Format(database.TimeFormat),
			strconv.FormatInt(conn.Scanner, 10),
			strconv.FormatInt(conn.Advertiser, 10),
			strconv.FormatInt(int64(conn.Power), 10),
			strconv.FormatInt(int64(conn.Rssi), 10),
		})

		if JsonFail(c, err) {
			return
		}
	}

	writer.Flush()
}

func GetDescription(c *gin.Context) {
	description, err := database.GetDescription(c.Param("experiment"))
	StringInfer(c, description, err)
}

func GetPrivacyPolicy(c *gin.Context) {
	policy, err := database.GetPrivacyPolicy(c.Param("experiment"))
	StringInfer(c, policy, err)
}

func NewExperimentBrowser(c *gin.Context) {
	if Release {
		password, ok := c.GetQuery("password")
		if !ok {
			JsonFail(c, MissingPassword)
			return
		}

		hashed, err := utils.HashPassword(password)
		if JsonFail(c, err) {
			return
		}

		if hashed != PasswordHash {
			JsonFail(c, IncorrectPassword)
			return
		}
	}

	password, ok := c.GetQuery("id")
	if !ok {
		password = utils.RandomString(127)
	}

	policy := c.Query("policy")
	description := c.Query("description")

	type Response struct {
		Password string `json:"password"`
		Id       uint32 `json:"id"`
	}

	id, err := database.InsertExperiment(password, policy, description, sql.NullTime{})
	JsonInfer(c, Response{password, id}, err)
}

func NewExperiment(c *gin.Context) {
	if Release {
		password, ok := c.GetPostForm("password")
		if !ok {
			JsonFail(c, MissingPassword)
			return
		}

		hashed, err := utils.HashPassword(password)
		if JsonFail(c, err) {
			return
		}

		if hashed != PasswordHash {
			JsonFail(c, IncorrectPassword)
			return
		}
	}

	password, ok := c.GetPostForm("id")
	if !ok {
		password = utils.RandomString(127)
	}

	policy := c.PostForm("policy")
	description := c.PostForm("description")

	type Response struct {
		Password string `json:"password"`
		Id       uint32 `json:"id"`
	}

	id, err := database.InsertExperiment(password, policy, description, sql.NullTime{})
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
