package main

import (
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/web"
	"github.com/gin-gonic/gin"
	"log"
)

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)
	router := gin.Default()

	database.ConnectToDb(database.DefaultURL)
	database.Clear()
	router.POST("/addUser", web.NewUser)
	router.POST("/addConnections", web.AddConnections)
	router.Run(":8080")
}
