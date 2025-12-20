package com.app.zionengine.camera

import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.input.KeyCode
import javafx.scene.transform.Rotate
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin


class CameraController(
    private val pivot: Group,
    private val camera: PerspectiveCamera
) {
    private val teclas = mutableSetOf<KeyCode>()

    // Estado da câmera
    private var yaw = -45.0
    private var pitch = -35.0
    private var zoom = -900.0

    // Limites
    private val pitchMin = -85.0
    private val pitchMax = 0.0
    private val zoomMin = -200.0
    private val zoomMax = -3000.0

    // Velocidades
    private val velocidadeBase = 300.0
    private val multiplicadorShift = 3.0
    private val velocidadeRotacao = 90.0
    private val velocidadeZoom = 800.0

    private val rotateX = Rotate(pitch, Rotate.X_AXIS)
    private val rotateY = Rotate(yaw, Rotate.Y_AXIS)

    init {
        pivot.transforms.addAll(rotateX, rotateY)
        pivot.children.add(camera)
    }

    fun handleKeyPress(key: KeyCode) {
        teclas.add(key)
    }

    fun handleKeyRelease(key: KeyCode) {
        teclas.remove(key)
    }

    fun update(delta: Double) {
        atualizarMovimento(delta)
        atualizarRotacao(delta)
        atualizarZoom(delta)
    }

    private fun atualizarMovimento(delta: Double) {
        var dx = 0.0
        var dz = 0.0
        var velocidadeAtual = velocidadeBase
        if (KeyCode.SHIFT in teclas) velocidadeAtual *= multiplicadorShift

        if (KeyCode.W in teclas) dz += velocidadeAtual * delta
        if (KeyCode.S in teclas) dz -= velocidadeAtual * delta
        if (KeyCode.A in teclas) dx -= velocidadeAtual * delta
        if (KeyCode.D in teclas) dx += velocidadeAtual * delta

        if (dx != 0.0 || dz != 0.0) moverCamera(dx, dz)
    }

    private fun atualizarRotacao(delta: Double) {
        if (KeyCode.J in teclas) yaw -= velocidadeRotacao * delta
        if (KeyCode.K in teclas) yaw += velocidadeRotacao * delta
        if (KeyCode.I in teclas) pitch -= velocidadeRotacao * delta
        if (KeyCode.U in teclas) pitch += velocidadeRotacao * delta

        pitch = pitch.coerceIn(pitchMin, pitchMax)
        rotateY.angle = yaw
        rotateX.angle = pitch
    }

    private fun atualizarZoom(delta: Double) {
        if (KeyCode.L in teclas) zoom -= velocidadeZoom * delta
        if (KeyCode.O in teclas) zoom += velocidadeZoom * delta
        zoom = zoom.coerceIn(zoomMax, zoomMin)
        camera.translateZ = zoom
    }

    private fun moverCamera(dx: Double, dz: Double) {
        val rad = toRadians(-yaw)
        val sinVal = sin(rad)
        val cosVal = cos(rad)

        pivot.translateX += dx * cosVal - dz * sinVal
        pivot.translateZ += dx * sinVal + dz * cosVal
    }
}