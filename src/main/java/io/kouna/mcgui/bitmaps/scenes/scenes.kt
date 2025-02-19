package io.kouna.mcgui.bitmaps.scenes

import io.kouna.mcgui.bitmaps.FragmentData
import io.kouna.mcgui.bitmaps.RenderBuffer
import io.kouna.mcgui.utilities.Grid
import org.bukkit.Color

val CLEAR_COLOR = Color.fromARGB(50, 0, 0, 0)

interface Scene {
    fun getBitmap(): Grid<Color>
    fun update() {}
}

class EmptyScene : Scene {
    val buffer = RenderBuffer(64, 64) { FragmentData(CLEAR_COLOR) }
    override fun getBitmap() = buffer.map { it.color }
}

