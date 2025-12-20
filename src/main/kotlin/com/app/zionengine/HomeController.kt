package com.app.zionengine

import javafx.fxml.FXML
import javafx.scene.*
import javafx.scene.image.WritableImage
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

    private fun criarSceneEditor(scene: Scene) {
        val root3D = Group()

        // === LUZ (essencial para não ficar opaco) ===
        val ambientLight = AmbientLight(Color.rgb(130, 130, 130))

        val directionalLight = DirectionalLight(Color.WHITE).apply {
            transforms.add(
                Rotate(-60.0, Rotate.X_AXIS)
            )
        }

        root3D.children.addAll(
            ambientLight,
            directionalLight,
            criarChao()
        )

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

    // === TEXTURA DE GRID FINA ===
    private fun criarTexturaGrid(
        tamanho: Int = 1024,
        passo: Int = 80
    ): WritableImage {

        val image = WritableImage(tamanho, tamanho)
        val pw = image.pixelWriter

        val corChao = Color.rgb(165, 165, 165)
        val corLinha = Color.rgb(120, 120, 120)

        for (x in 0 until tamanho) {
            for (y in 0 until tamanho) {

                val linhaFina =
                    x % passo == 0 ||
                            y % passo == 0

                pw.setColor(
                    x,
                    y,
                    if (linhaFina) corLinha else corChao
                )
            }
        }

        return image
    }

    private fun criarCameraEditor(): PerspectiveCamera {
        return PerspectiveCamera(true).apply {

            translateX = 200.0
            translateY = -2000.0
            translateZ = -700.0

            transforms.addAll(
                Rotate(-50.0, Rotate.X_AXIS),
                Rotate(0.0, Rotate.Y_AXIS)
            )

            nearClip = 0.1
            farClip = 10_000.0
        }
    }

    // === CHÃO COM GRID EMBUTIDO ===
    private fun criarChao(
        tamanho: Double = 2000.0
    ): Box {

        val material = PhongMaterial().apply {
            diffuseMap = criarTexturaGrid()
            diffuseColor = Color.rgb(200, 200, 200)
            specularColor = Color.rgb(180, 180, 180)
            specularPower = 32.0
        }

        return Box(tamanho, 1.0, tamanho).apply {
            translateY = 0.6
            this.material = material
        }
    }
}
