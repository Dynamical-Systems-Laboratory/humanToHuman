package main

import (
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/web"
	"github.com/gin-gonic/gin"
	"log"
)

type Flags struct {
	Release   bool
	Password  *string
	Dbconnstr string
	Port      *string
}

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)
	var flags Flags

	flags.Dbconnstr = "user=humantohuman password=humantohuman sslmode=disable host=localhost dbname=humantohuman"

	err := utils.ArgParseGlobal(&flags)
	utils.FailIf(err, "Argument parsing failed")

	port := ":8080"
	if flags.Port != nil {
		port = *flags.Port
	}

	if flags.Release && flags.Password == nil {
		utils.Fail("password required for initial authentification!")
	}

	if flags.Release {
		gin.SetMode(gin.ReleaseMode)
		web.Release = true
		var err error
		web.PasswordHash, err = utils.HashPassword(*flags.Password)
		utils.FailIf(err, "failed to hash provided password")
	}

	router := gin.Default()

	database.ConnectToDb(flags.Dbconnstr)

	router.GET("/clear", func(c *gin.Context) {
		web.JsonInfer(c, nil, database.Clear())
	})
	router.GET("/clearConnections", func(c *gin.Context) {
		web.JsonInfer(c, nil, database.ClearConnections())
	})
	router.GET("/addExperiment", web.NewExperimentBrowser)
	router.POST("/addExperiment", web.NewExperiment)

	router.GET("/experiment/:experiment/data.csv", web.GetCSV)
	router.GET("/experiment/:experiment/policy", web.GetPrivacyPolicy)
	router.GET("/experiment/:experiment/description", web.GetDescription)
	router.POST("/experiment/:experiment/addUser", web.NewUser)
	router.POST("/experiment/:experiment/addConnections", web.AddConnectionsUnsafe)
	router.POST("/experiment/:experiment/addConnectionsUnsafe", web.AddConnectionsUnsafe)

	router.Run(port)
}
