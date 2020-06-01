package web

import (
	"errors"
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
		c.JSON(400, ErrorApiMessage{400, err.Error()})
	} else {
		c.JSON(200, object)
	}
}

func JsonFail(c *gin.Context, err error) bool {
	if err != nil {
		c.JSON(400, ErrorApiMessage{400, err.Error()})
		return true
	}
	return false
}
