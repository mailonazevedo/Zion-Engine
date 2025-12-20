package com.app.zionengine

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

    private lateinit var subScene3D: SubScene

    // === CAMERA ===
    private lateinit var cameraPivot: Group
    private val teclas = mutableSetOf<KeyCode>()
    private val velocidadeBase = 300.0 // unidades por segundo

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

        // === LUZ ===
        val ambientLight = AmbientLight(Color.rgb(130, 130, 130))

        val directionalLight = DirectionalLight(Color.WHITE).apply {
            transforms.add(Rotate(-60.0, Rotate.X_AXIS))
        }

        root3D.children.addAll(
            ambientLight,
            directionalLight,
            criarChao()
        )

        val camera = criarCameraEditor()
        root3D.children.add(cameraPivot)

        subScene3D = SubScene(
            root3D,
            0.0,
            0.0,
            true,
            SceneAntialiasing.BALANCED
        )

        subScene3D.fill = Color.rgb(32, 32, 32)
        subScene3D.camera = camera

        subScene3D.widthProperty().bind(scene.widthProperty())
        subScene3D.heightProperty().bind(scene.heightProperty().multiply(0.8))

        viewport3D.children.setAll(subScene3D)

        // === INPUT ===
        scene.setOnKeyPressed { teclas.add(it.code) }
        scene.setOnKeyReleased { teclas.remove(it.code) }

        iniciarLoopMovimento()
    }

    // =========================================================
    // LOOP DE ATUALIZAÇÃO (MOVIMENTO FLUIDO)
    // =========================================================
    private fun iniciarLoopMovimento() {

        object : AnimationTimer() {

            private var ultimoTempo = 0L

            override fun handle(agora: Long) {

                if (ultimoTempo == 0L) {
                    ultimoTempo = agora
                    return
                }

                val deltaTime = (agora - ultimoTempo) / 1_000_000_000.0
                ultimoTempo = agora

                atualizarMovimento(deltaTime)
            }
        }.start()
    }

    private fun atualizarMovimento(delta: Double) {

        var dx = 0.0
        var dz = 0.0

        if (KeyCode.W in teclas) dz += velocidadeBase * delta
        if (KeyCode.S in teclas) dz -= velocidadeBase * delta
        if (KeyCode.A in teclas) dx -= velocidadeBase * delta
        if (KeyCode.D in teclas) dx += velocidadeBase * delta

        if (dx != 0.0 || dz != 0.0) {
            moverCamera(dx, dz)
        }
    }

    // =========================================================
    // MOVIMENTO BASEADO NA ROTAÇÃO DA CÂMERA
    // =========================================================
    private fun moverCamera(dx: Double, dz: Double) {

        val rotY = cameraPivot.transforms
            .filterIsInstance<Rotate>()
            .first { it.axis == Rotate.Y_AXIS }
            .angle

        val rad = Math.toRadians(-rotY)

        val sin = Math.sin(rad)
        val cos = Math.cos(rad)

        cameraPivot.translateX += dx * cos - dz * sin
        cameraPivot.translateZ += dx * sin + dz * cos
    }

    // =========================================================
    // CAMERA COM PIVOT
    // =========================================================
    private fun criarCameraEditor(): PerspectiveCamera {

        cameraPivot = Group()

        val camera = PerspectiveCamera(true).apply {
            translateZ = -900.0
            nearClip = 0.1
            farClip = 10_000.0
        }

        cameraPivot.transforms.addAll(
            Rotate(-35.0, Rotate.X_AXIS),
            Rotate(-45.0, Rotate.Y_AXIS)
        )

        cameraPivot.children.add(camera)

        return camera
    }

    // =========================================================
    // CHÃO COM GRID EMBUTIDO
    // =========================================================
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

    // =========================================================
    // TEXTURA DO GRID
    // =========================================================
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

                val linha =
                    x % passo == 0 || y % passo == 0

                pw.setColor(
                    x,
                    y,
                    if (linha) corLinha else corChao
                )
            }
        }

        return image
    }
}
