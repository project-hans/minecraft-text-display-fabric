package io.kouna.mcgui.bitmaps.shaders

import io.kouna.mcgui.bitmaps.*
import org.bukkit.Color
import org.joml.Vector4d


object RGB2DShader : ShaderProgram {
    override val vertex: VertexShader =  fun (vertex: DoubleArray): VertexData {
        if (vertex.size != 5) throw java.lang.Error("RGBAVertex shader expects exactly 5 elements")
        val x = vertex[0];
        val y = vertex[1];
        val r = vertex[2];
        val g = vertex[3];
        val b = vertex[4];

        val position = Vector4d(x,y, 0.0, 1.0)
        val pixel = doubleArrayOf(r,g,b)
        return VertexData(position, pixel)
    }

    override val fragment: FragmentShader = fun (pixel: DoubleArray): FragmentData {
        val r = pixel[0] * 255
        val g = pixel[1] * 255
        val b = pixel[2] * 255

        return FragmentData(Color.fromARGB(255, r.toInt(), g.toInt(), b.toInt()))
    }
}