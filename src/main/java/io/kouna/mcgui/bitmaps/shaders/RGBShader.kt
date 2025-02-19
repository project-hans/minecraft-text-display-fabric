package io.kouna.mcgui.bitmaps.shaders

import io.kouna.mcgui.bitmaps.*
import org.bukkit.Color
import org.joml.Matrix4d
import org.joml.Vector4d

object RGBShader: ShaderProgram {
    var transform = Matrix4d()

    override val vertex: VertexShader = fun(vertex: DoubleArray): VertexData {
        if (vertex.size != 6) throw Error("RGBAVertex shader expects exactly 6 elements")
        val x = vertex[0];
        val y = vertex[1];
        val z = vertex[2];
        val r = vertex[3];
        val g = vertex[4];
        val b = vertex[5];

        val position = transform.transform(Vector4d(x, y, z, 1.0))

        val pixel = doubleArrayOf(r, g, b)
        return VertexData(position, pixel)
    }

    override val fragment: FragmentShader = fun(pixel: DoubleArray): FragmentData {
        val r = pixel[0] * 255
        val g = pixel[1] * 255
        val b = pixel[2] * 255

        return FragmentData(Color.fromARGB(255, r.toInt(), g.toInt(), b.toInt()))
    }
}