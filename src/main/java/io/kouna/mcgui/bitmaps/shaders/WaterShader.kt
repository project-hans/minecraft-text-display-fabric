package io.kouna.mcgui.bitmaps.shaders

import io.kouna.mcgui.bitmaps.*
import io.kouna.mcgui.utilities.Grid
import io.kouna.mcgui.utilities.blendAlpha
import org.bukkit.Color
import org.joml.Matrix4d
import org.joml.Vector4d
import kotlin.math.sin

object WaterShader: ShaderProgram {
    var emptyReflectionTexture = Grid(1, 1) { FragmentData(Color.fromARGB(0, 0, 0, 0)) }

    var transform = Matrix4d()
    var reflectionTexture = emptyReflectionTexture
    var distortionFrame = 0.0
    var distortionMagnitude = 0.03
    var distortionRate = 2.0
    var distortionPeriods = 100.0
    var baseColor = Color.fromARGB((0.7 * 255).toInt(), (0.3 * 255).toInt(), (0.5 * 255).toInt(), (0.7 * 255).toInt())
    var tintColor = baseColor.setAlpha((255 * .3).toInt())

    override val vertex: VertexShader = fun (vertex): VertexData {
        if (vertex.size != 5) throw java.lang.Error("WaterVertex shader expects exactly 5 elements")
        val x: Double = vertex[0]
        val y: Double = vertex[1]
        val z: Double = vertex[2]
        val u: Double = vertex[3]
        val v: Double = vertex[4]

        val position = transform.transform(Vector4d(x,y,z,1.0))
        val pixel = doubleArrayOf(u, v)
        return VertexData(position, pixel)
    }

    override val fragment: FragmentShader = fun (pixel): FragmentData {
        val u: Double = pixel[0]
        val v: Double = pixel[1]

        // wave distortion effect
        val distortion = distortionMagnitude *
                sin(distortionRate * distortionFrame) *
                sin(v * distortionPeriods)

        // sample from reflection texture
        val reflection = reflectionTexture.getFraction(u + distortion, v).color

        // add tint to sample
        val fragColor = (if (reflection.alpha == 0) baseColor else reflection.setAlpha(baseColor.alpha)).blendAlpha(tintColor)
        return FragmentData(fragColor)
    }
}