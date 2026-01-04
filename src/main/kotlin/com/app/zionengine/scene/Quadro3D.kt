package com.app.zionengine.scene

import javafx.animation.AnimationTimer
import javafx.scene.AmbientLight
import javafx.scene.DirectionalLight
import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.SubScene
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.PhongMaterial
import javafx.scene.paint.Stop
import javafx.scene.shape.Box
import javafx.scene.shape.CullFace
import javafx.scene.shape.DrawMode
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Rotate

class Quadro3D : IQuadro3D {

    private lateinit var subScene3D: SubScene

    // === CAMERA ===
    private lateinit var cameraPivot: Group
    private lateinit var camera: PerspectiveCamera
    private lateinit var rotateY: Rotate
    private lateinit var rotateX: Rotate

    private val teclas = mutableSetOf<KeyCode>()

    private val velocidadeBase = 300.0
    private val multiplicadorShift = 3.0
    private val velocidadeRotacao = 90.0
    private val velocidadeZoom = 800.0

    private var yaw = -45.0
    private var pitch = -35.0
    private var zoom = -900.0

    // Cache para otimização trigonométrica
    private var cachedSin = 0.0
    private var cachedCos = 0.0
    private var lastYaw = Double.NaN

    private val pitchMin = -85.0
    private val pitchMax = 0.0

    private val zoomMin = -200.0
    private val zoomMax = -3000.0


    override fun criarSceneEditor(scene: Scene, viewport3D: StackPane) {

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

        subScene3D.fill = Color.rgb(100, 140, 190) // azul Unity
        subScene3D.camera = camera

        subScene3D.widthProperty().bind(scene.widthProperty())
        subScene3D.heightProperty().bind(scene.heightProperty().multiply(0.8))

        viewport3D.children.setAll(subScene3D)

        scene.setOnKeyPressed { teclas.add(it.code) }
        scene.setOnKeyReleased { teclas.remove(it.code) }

        iniciarLoop()
    }

    override fun iniciarLoop() {
        object : AnimationTimer() {

            private var ultimoTempo = 0L
            private var skipFrames = 0

            override fun handle(agora: Long) {

                if (ultimoTempo == 0L) {
                    ultimoTempo = agora
                    return
                }

                val delta = (agora - ultimoTempo) / 1_000_000_000.0
                ultimoTempo = agora

                // Limita delta para evitar pulos grandes quando há lag
                val deltaSuavizado = delta.coerceAtMost(0.033) // máximo 33ms (~30fps)

                atualizarMovimento(deltaSuavizado)
                atualizarRotacao(deltaSuavizado)
                atualizarZoom(deltaSuavizado)
            }
        }.start()
    }

    override fun atualizarMovimento(delta: Double) {

        // Early return se não há movimento
        if (KeyCode.W !in teclas && KeyCode.S !in teclas &&
            KeyCode.A !in teclas && KeyCode.D !in teclas) {
            return
        }

        var dx = 0.0
        var dz = 0.0

        val velocidadeAtual = if (KeyCode.SHIFT in teclas) {
            velocidadeBase * multiplicadorShift
        } else {
            velocidadeBase
        }

        if (KeyCode.W in teclas) dz += velocidadeAtual * delta
        if (KeyCode.S in teclas) dz -= velocidadeAtual * delta
        if (KeyCode.A in teclas) dx -= velocidadeAtual * delta
        if (KeyCode.D in teclas) dx += velocidadeAtual * delta

        moverCamera(dx, dz)
    }

    override fun atualizarRotacao(delta: Double) {

        var rotacaoAlterada = false

        // === YAW (J / K) ===
        if (KeyCode.J in teclas) {
            yaw -= velocidadeRotacao * delta
            rotacaoAlterada = true
        }
        if (KeyCode.K in teclas) {
            yaw += velocidadeRotacao * delta
            rotacaoAlterada = true
        }

        // === PITCH (I / U) ===
        if (KeyCode.I in teclas) {
            pitch -= velocidadeRotacao * delta
            rotacaoAlterada = true
        }
        if (KeyCode.U in teclas) {
            pitch += velocidadeRotacao * delta
            rotacaoAlterada = true
        }

        // Só atualiza se houve mudança
        if (rotacaoAlterada) {
            pitch = pitch.coerceIn(pitchMin, pitchMax)
            rotateY.angle = yaw
            rotateX.angle = pitch
        }
    }

    // =========================================================
    // ZOOM (L / O)
    // =========================================================
    override fun atualizarZoom(delta: Double) {

        var zoomAlterado = false

        if (KeyCode.L in teclas) {
            zoom -= velocidadeZoom * delta
            zoomAlterado = true
        }
        if (KeyCode.O in teclas) {
            zoom += velocidadeZoom * delta
            zoomAlterado = true
        }

        // Só atualiza se houve mudança
        if (zoomAlterado) {
            zoom = zoom.coerceIn(zoomMax, zoomMin)
            camera.translateZ = zoom
        }
    }

    override fun moverCamera(dx: Double, dz: Double) {

        // Cache de cálculos trigonométricos para evitar recálculo
        if (yaw != lastYaw) {
            val rad = Math.toRadians(-yaw)
            cachedSin = Math.sin(rad)
            cachedCos = Math.cos(rad)
            lastYaw = yaw
        }

        cameraPivot.translateX += dx * cachedCos - dz * cachedSin
        cameraPivot.translateZ += dx * cachedSin + dz * cachedCos
    }


    override fun criarCameraEditor(): PerspectiveCamera {

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
    override fun criarChao(tamanho: Double, altura: Double): Box{

        val material = PhongMaterial().apply {
            diffuseMap = criarTexturaGrid(4096, 64) // textura ultra HD para máxima nitidez
            diffuseColor = Color.rgb(200, 200, 200) // cinza claro
            specularColor = Color.rgb(180, 180, 180) // reflexo sutil
            specularPower = 16.0                     // reflexo mais difuso (mais realista)
        }

        return Box(tamanho, altura, tamanho).apply {
            translateY = altura / 2.0  // centraliza o chão em relação ao eixo Y
            this.material = material
        }
    }

    // =========================================================
    // TEXTURA GRID
    // =========================================================
    override fun criarTexturaGrid(
        tamanho: Int,
        passo: Int
    ): WritableImage {

        val image = WritableImage(tamanho, tamanho)
        val pw = image.pixelWriter

        // Cores estilo Unreal Engine - tons de cinza claro
        val corChao = Color.rgb(180, 180, 180)        // cinza claro
        val corLinhaFina = Color.rgb(150, 150, 150)   // linhas finas do grid
        val corLinhaGrossa = Color.rgb(120, 120, 120) // linhas principais (a cada 10)
        val corEixos = Color.rgb(90, 90, 90)          // linhas de eixo central

        val centro = tamanho / 2

        // Supersampling 2x2 para anti-aliasing de alta qualidade
        for (x in 0 until tamanho) {
            for (y in 0 until tamanho) {

                var r = 0.0
                var g = 0.0
                var b = 0.0

                // Amostra 4 sub-pixels para cada pixel
                for (sx in 0..1) {
                    for (sy in 0..1) {
                        val sampleX = x + sx * 0.5
                        val sampleY = y + sy * 0.5

                        val linhaFina = (sampleX % passo < 1.5) || (sampleY % passo < 1.5)
                        val linhaGrossa = (sampleX % (passo * 10) < 2.5) || (sampleY % (passo * 10) < 2.5)
                        val eixoCentral = (sampleX >= centro - 1.5 && sampleX <= centro + 1.5) ||
                                         (sampleY >= centro - 1.5 && sampleY <= centro + 1.5)

                        val cor = when {
                            eixoCentral -> corEixos
                            linhaGrossa -> corLinhaGrossa
                            linhaFina -> corLinhaFina
                            else -> corChao
                        }

                        r += cor.red
                        g += cor.green
                        b += cor.blue
                    }
                }

                // Média das 4 amostras
                pw.setColor(x, y, Color.color(r / 4.0, g / 4.0, b / 4.0))
            }
        }

        return image
    }
}