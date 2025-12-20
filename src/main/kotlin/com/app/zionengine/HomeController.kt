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
    private lateinit var camera: PerspectiveCamera
    private lateinit var rotateY: Rotate
    private lateinit var rotateX: Rotate

    private val teclas = mutableSetOf<KeyCode>()

    // === VELOCIDADES ===
    private val velocidadeBase = 300.0
    private val multiplicadorShift = 3.0
    private val velocidadeRotacao = 90.0
    private val velocidadeZoom = 800.0

    // === ESTADO DA CAMERA ===
    private var yaw = -45.0
    private var pitch = -35.0
    private var zoom = -900.0

    // === LIMITES ===
    private val pitchMin = -85.0
    private val pitchMax = 0.0

    private val zoomMin = -200.0
    private val zoomMax = -3000.0

    @FXML
    fun initialize() {
        viewport3D.sceneProperty().addListener { _, _, scene ->
            if (scene != null) criarSceneEditor(scene)
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

        camera = criarCameraEditor()
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

        scene.setOnKeyPressed { teclas.add(it.code) }
        scene.setOnKeyReleased { teclas.remove(it.code) }

        iniciarLoop()
    }

    // =========================================================
    // LOOP PRINCIPAL
    // =========================================================
    private fun iniciarLoop() {
        object : AnimationTimer() {

            private var ultimoTempo = 0L

            override fun handle(agora: Long) {

                if (ultimoTempo == 0L) {
                    ultimoTempo = agora
                    return
                }

                val delta = (agora - ultimoTempo) / 1_000_000_000.0
                ultimoTempo = agora

                atualizarMovimento(delta)
                atualizarRotacao(delta)
                atualizarZoom(delta)
            }
        }.start()
    }

    // =========================================================
    // MOVIMENTO (WASD + SHIFT)
    // =========================================================
    private fun atualizarMovimento(delta: Double) {

        var dx = 0.0
        var dz = 0.0

        var velocidadeAtual = velocidadeBase
        if (KeyCode.SHIFT in teclas) {
            velocidadeAtual *= multiplicadorShift
        }

        if (KeyCode.W in teclas) dz += velocidadeAtual * delta
        if (KeyCode.S in teclas) dz -= velocidadeAtual * delta
        if (KeyCode.A in teclas) dx -= velocidadeAtual * delta
        if (KeyCode.D in teclas) dx += velocidadeAtual * delta

        if (dx != 0.0 || dz != 0.0) moverCamera(dx, dz)
    }

    // =========================================================
    // ROTAÇÃO (YAW + PITCH COM LIMITE)
    // =========================================================
    private fun atualizarRotacao(delta: Double) {

        // === YAW (J / K) ===
        if (KeyCode.J in teclas) yaw -= velocidadeRotacao * delta
        if (KeyCode.K in teclas) yaw += velocidadeRotacao * delta

        // === PITCH (I / U) ===
        if (KeyCode.I in teclas) pitch -= velocidadeRotacao * delta
        if (KeyCode.U in teclas) pitch += velocidadeRotacao * delta

        pitch = pitch.coerceIn(pitchMin, pitchMax)

        rotateY.angle = yaw
        rotateX.angle = pitch
    }

    // =========================================================
    // ZOOM (L / O)
    // =========================================================
    private fun atualizarZoom(delta: Double) {

        if (KeyCode.L in teclas) zoom -= velocidadeZoom * delta
        if (KeyCode.O in teclas) zoom += velocidadeZoom * delta

        zoom = zoom.coerceIn(zoomMax, zoomMin)
        camera.translateZ = zoom
    }

    // =========================================================
    // MOVIMENTO RELATIVO AO YAW
    // =========================================================
    private fun moverCamera(dx: Double, dz: Double) {

        val rad = Math.toRadians(-yaw)
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

        rotateX = Rotate(pitch, Rotate.X_AXIS)
        rotateY = Rotate(yaw, Rotate.Y_AXIS)

        cameraPivot.transforms.addAll(rotateX, rotateY)

        camera = PerspectiveCamera(true).apply {
            translateZ = zoom
            nearClip = 0.1
            farClip = 10_000.0
        }

        cameraPivot.children.add(camera)
        return camera
    }

    // =========================================================
    // CHÃO COM GRID
    // =========================================================
    private fun criarChao(tamanho: Double = 2000.0): Box {

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
    // TEXTURA GRID
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
                val linha = x % passo == 0 || y % passo == 0
                pw.setColor(x, y, if (linha) corLinha else corChao)
            }
        }

        return image
    }
}
