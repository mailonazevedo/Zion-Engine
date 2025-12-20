package com.app.zionengine.scene

import com.app.zionengine.camera.CameraController
import javafx.scene.*
import javafx.scene.paint.Color
import javafx.scene.transform.Rotate

class SceneBuilder3D {

    lateinit var cameraController: CameraController
        private set

    fun criarScene(): SubScene {
        val root3D = Group()

        // === Luzes ===
        root3D.children.addAll(
            criarAmbientLight(),
            criarDirectionalLight(),
            FloorFactory.criarChao()
        )

        // === Câmera ===
        val cameraPivot = Group()
        val camera = PerspectiveCamera(true)
        cameraController = CameraController(cameraPivot, camera)
        root3D.children.add(cameraPivot)

        // === SubScene ===
        return SubScene(root3D, 0.0, 0.0, true, SceneAntialiasing.BALANCED).apply {
            fill = Color.rgb(32, 32, 32)
            this.camera = camera
        }
    }

    private fun criarAmbientLight() = AmbientLight(Color.rgb(130, 130, 130))

    private fun criarDirectionalLight(): DirectionalLight {
        return DirectionalLight(Color.WHITE).apply {
            transforms.add(Rotate(-60.0, Rotate.X_AXIS))
        }
    }
}