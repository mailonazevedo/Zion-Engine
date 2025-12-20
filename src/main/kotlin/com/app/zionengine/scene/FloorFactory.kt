package com.app.zionengine.scene

import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.Box

object FloorFactory {

    fun criarChao(tamanho: Double = 2000.0, altura: Double = 10.0): Box {
        val material = PhongMaterial().apply {
            diffuseMap = criarTexturaGrid()
            diffuseColor = Color.rgb(200, 200, 200)
            specularColor = Color.rgb(180, 180, 180)
            specularPower = 32.0
        }

        return Box(tamanho, altura, tamanho).apply {
            translateY = altura / 2.0 // centraliza o chão
            this.material = material
        }
    }

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