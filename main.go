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
	router.GET("/clear", func(ctx *gin.Context) {
		database.Clear()
	})
	router.GET("/addExperiment", web.NewExperimentBrowser)
	router.POST("/addExperiment", web.NewExperiment)
	router.POST("/experiment/:experiment/policy", web.GetPrivacyPolicy)
	router.POST("/experiment/:experiment/description", web.GetDescription)
	router.POST("/experiment/:experiment/addUser", web.NewUser)
	router.POST("/experiment/:experiment/addConnections", web.AddConnectionsUnsafe)
	router.Run(":8080")
}
