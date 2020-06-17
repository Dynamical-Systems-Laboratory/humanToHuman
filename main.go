package main

import (
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/web"
	"github.com/gin-gonic/gin"
	"log"
)

type Flags struct {
	Release  bool
	Password *string
}

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)
	var flags Flags
	err := utils.ArgParseGlobal(&flags)
	utils.FailIf(err, "Argument parsing failed")
	utils.Print("%v", err)
	if flags.Release && flags.Password == nil {
		utils.Fail("password required for initial authentification!")
	}
	if flags.Release {
		gin.SetMode(gin.ReleaseMode)
	}

	router := gin.Default()

	database.ConnectToDb(database.DefaultURL)
	database.ClearConnections()
	router.POST("/addUser", web.NewUser)
	router.POST("/addConnections", web.AddConnectionsUnsafe)
	router.Run(":8080")
}
