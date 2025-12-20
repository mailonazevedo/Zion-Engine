package com.app.zionengine

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.input.KeyCombination
import javafx.stage.Stage

class HomeApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HomeApplication::class.java.getResource("home-view.fxml"))
        val scene = Scene(fxmlLoader.load())
        stage.title = "Zion Engine"
        stage.scene = scene
        stage.fullScreenExitHint = ""
        stage.isFullScreen = true
        stage.fullScreenExitKeyCombination =
            KeyCombination.valueOf("F11")

        stage.show()
    }
}
  
