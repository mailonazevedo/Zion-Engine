package com.app.zionengine

import javafx.fxml.FXML
import javafx.scene.*
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.Box
import javafx.scene.transform.Rotate

class HomeController {

    @FXML
    lateinit var viewport3D: StackPane

    private lateinit var subScene3D: SubScene

    @FXML
    fun initialize() {
        viewport3D.sceneProperty().addListener { _, _, scene ->
            if (scene != null) {
                criarSceneEditor(scene)
            }
        }
    }

    // =========================
    // SCENE DE EDITOR (UNITY-LIKE)
    // =========================
    private fun criarSceneEditor(scene: Scene) {
        val root3D = Group()

        // Chão cinza
        root3D.children.add(criarChao())
        // Grid 3D (plano XZ)
        root3D.children.add(criarGrid3D())

        val camera = criarCameraEditor()

        subScene3D = SubScene(
            root3D,
            0.0,
            0.0,
            true,
            SceneAntialiasing.BALANCED
        )

        subScene3D.fill = Color.rgb(32, 32, 32)
        subScene3D.camera = camera

        // 100% largura / 80% altura
        subScene3D.widthProperty().bind(scene.widthProperty())
        subScene3D.heightProperty().bind(scene.heightProperty().multiply(0.8))

        viewport3D.children.setAll(subScene3D)
    }

    // =========================
    // GRID 3D (USANDO BOX COMO LINHA)
    // =========================
    private fun criarGrid3D(
        tamanho: Int = 1000,
        passo: Int = 50
    ): Group {

        val grid = Group()

        val gridMaterial = PhongMaterial(Color.rgb(70, 70, 70))

        for (i in -tamanho..tamanho step passo) {

            // Linha paralela ao eixo X
            val linhaX = Box(
                tamanho * 2.0,
                0.5,
                0.5
            ).apply {
                translateZ = i.toDouble()
                material = gridMaterial
            }

            // Linha paralela ao eixo Z
            val linhaZ = Box(
                0.5,
                0.5,
                tamanho * 2.0
            ).apply {
                translateX = i.toDouble()
                material = gridMaterial
            }

            grid.children.addAll(linhaX, linhaZ)
        }

        return grid
    }

    // =========================
    // CÂMERA DE EDITOR (UNITY STYLE)
    // =========================
    private fun criarCameraEditor(): PerspectiveCamera {
        return PerspectiveCamera(true).apply {

            translateX = -400.0
            translateY = -300.0
            translateZ = -400.0

            transforms.addAll(
                Rotate(-35.0, Rotate.X_AXIS),
                Rotate(-45.0, Rotate.Y_AXIS)
            )

            nearClip = 0.1
            farClip = 10_000.0
        }
    }
    private fun criarChao(
        tamanho: Double = 2000.0
    ): Box {

        return Box(tamanho, 1.0, tamanho).apply {
            translateY = 0.5  // levemente abaixo do grid
            material = PhongMaterial(Color.rgb(180, 180, 180))
        }
    }
}
