package com.app.zionengine

import com.app.zionengine.scene.Quadro3D
import javafx.animation.AnimationTimer
import javafx.fxml.FXML
import javafx.scene.*
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.Box
import javafx.scene.transform.Rotate

class HomeController {

    @FXML
    lateinit var viewport3D: StackPane

    val quadro3D = Quadro3D()

    @FXML
    fun initialize() {
        viewport3D.sceneProperty().addListener { _, _, scene ->
            if (scene != null) quadro3D.criarSceneEditor(scene, viewport3D)
        }
    }


}
