package com.app.zionengine.scene

import javafx.scene.PerspectiveCamera
import javafx.scene.Scene
import javafx.scene.image.WritableImage
import javafx.scene.layout.StackPane
import javafx.scene.shape.Box

interface IQuadro3D {

    fun criarSceneEditor(scene: Scene, viewport3D: StackPane)

    fun iniciarLoop()

    fun atualizarMovimento(delta: Double)

    fun atualizarRotacao(delta: Double)

    fun atualizarZoom(delta: Double)

    fun moverCamera(dx: Double, dz: Double)
    fun criarCameraEditor(): PerspectiveCamera

    fun criarChao(tamanho: Double = 2000.0, altura: Double = 10.0): Box

    fun criarTexturaGrid(tamanho: Int = 1024, passo: Int = 80): WritableImage
}