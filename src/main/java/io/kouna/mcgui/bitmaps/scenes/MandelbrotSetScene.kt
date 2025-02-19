package io.kouna.mcgui.bitmaps.scenes

import io.kouna.mcgui.utilities.*
import org.bukkit.Color
import org.joml.Vector2d
import kotlin.math.log2

class MandelbrotSetScene: Scene {
    private val height = 64
    private val bitmap = Grid((height * 2.0).toInt(), height) { CLEAR_COLOR }
    override fun getBitmap() = bitmap

    var viewRect = Rect.fromMinMax(
        min = Vector2d(-2.0, .0),
        max = Vector2d(.5  , .0),
    ).apply {
        setYCenter(.0, this.width * (bitmap.height.toDouble() / bitmap.width))
        expand(.5)
    }

    var viewRectAnchor = viewRect.clone()

    var maxIterationsBase = 50
    var maxIterationsScale = 25.0

    var zoomLevel = 1.0
    private fun maxIterations() = (maxIterationsBase + log2(zoomLevel) * maxIterationsScale).toInt()

    var maxIterations = maxIterations()

    private val originalWidth = viewRect.width
    override fun update() {
        zoomLevel = originalWidth / viewRect.width
        maxIterations = maxIterations()


        for ((px, py) in bitmap.indices()) {
            val x = viewRect.minX + viewRect.width * (px.toDouble() / bitmap.width)
            val y = viewRect.minY + viewRect.height * (py.toDouble() / bitmap.height)

            var a = x
            var b = y

            var i = 0
            while (i < maxIterations) {
                val a2 = a * a - b * b + x
                val b2 = 2 * a * b + y
                a = a2
                b = b2

                if (Vector2d(a,b).lengthSquared() > 4) break
                i += 1
            }

            bitmap[px to py] = colors.interpolateRGB(i.toDouble() / maxIterations)
        }
    }


    private val colors = listOf(
        0.0     to Color.fromRGB(  0,   7, 100),
        0.16    to Color.fromRGB( 32, 107, 203),
        0.42    to Color.fromRGB(237, 255, 255),
        0.6425  to Color.fromRGB(255, 170,   0),
        0.8575  to Color.fromRGB(  0,   0,   0),
    )
}