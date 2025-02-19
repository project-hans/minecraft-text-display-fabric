package io.kouna.mcgui.bitmaps.scenes

import io.kouna.mcgui.bitmaps.FragmentData
import io.kouna.mcgui.bitmaps.RenderBuffer
import io.kouna.mcgui.bitmaps.drawTriangles
import io.kouna.mcgui.bitmaps.meshes.RainbowTriangle
import io.kouna.mcgui.bitmaps.shaders.RGB2DShader

class RainbowTriangleScene : Scene {
    val buffer = RenderBuffer(64, 64) { FragmentData(CLEAR_COLOR) }
    override fun getBitmap() = buffer.map { it.color }
    init {
        buffer.drawTriangles(RainbowTriangle, RGB2DShader, true)
    }
}