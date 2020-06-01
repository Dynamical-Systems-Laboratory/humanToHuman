package main

import (
	"github.com/Dynamical-Systems-Laboratory/humanToHuman/database"
	_ "github.com/Dynamical-Systems-Laboratory/humanToHuman/test"
	_ "github.com/Dynamical-Systems-Laboratory/humanToHuman/utils"
	_ "github.com/Dynamical-Systems-Laboratory/humanToHuman/web"
	"github.com/gin-gonic/gin"
	"log"
)

func main() {
	log.SetFlags(log.LstdFlags | log.Lshortfile)
	router := gin.Default()

	database.ConnectToDb(database.DefaultURL)
	database.Clear()
	router.Run(":8080")
}
