package main

import (
	"flag"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/web"
	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/acme/autocert"
	"log"
	"net/http"
)

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)

	release := flag.Bool("release", false, "whether or not to run in release mode")
	password := flag.String("password", "", "password for the server")
	dbconnstr := flag.String("dbconnstr", "user=humantohuman password=humantohuman sslmode=disable host=localhost dbname=humantohuman", "database connection string")
	port := flag.String("port", "", "port of the server")
	domain := flag.String("domain", "", "domain of the server, to use for HTTPS")
	flag.Parse()

	if *release {
		if *password == "" {
			utils.Fail("password required for initial authentification!")
		}

		gin.SetMode(gin.ReleaseMode)
		web.Release = true
		var err error
		web.PasswordHash, err = utils.HashPassword(*password)
		utils.FailIf(err, "failed to hash provided password")
		*password = ""
	}

	router := gin.Default()

	database.ConnectToDb(*dbconnstr)

	router.GET("/clear", web.ClearBrowser)
	router.POST("/clear", web.Clear)
	router.GET("/addExperiment", web.NewExperimentBrowser)
	router.POST("/addExperiment", web.NewExperiment)

	router.GET("/experiment/:experiment/data.csv", web.GetCSV)
	router.GET("/experiment/:experiment/policy", web.GetPrivacyPolicy)
	router.GET("/experiment/:experiment/description", web.GetDescription)
	router.POST("/experiment/:experiment/addUser", web.NewUser)
	router.POST("/experiment/:experiment/removeUser", web.RemoveUser)
	router.POST("/experiment/:experiment/addConnections", web.AddConnectionsUnsafe)
	router.POST("/experiment/:experiment/addConnectionsUnsafe", web.AddConnectionsUnsafe)

	if (*port != "" && *port != ":443") || *domain == "" {
		if *domain != "" {
			utils.Fail("can't do https encryption on port other than 443")
		}
		if *port == "" {
			*port = ":8080"
		}

		err := router.Run(*port)
		utils.FailIf(err, "why did this fail?")
	} else {
		err := http.Serve(autocert.NewListener(*domain), router)
		utils.FailIf(err, "why did this fail?")
	}
}
