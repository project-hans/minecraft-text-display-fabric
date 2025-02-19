package io.kouna.mcgui.bitmaps.meshes

import io.kouna.mcgui.bitmaps.Mesh

object RainbowTriangle : Mesh {
	override val vertices = arrayOf(
		doubleArrayOf(0.0, 0.5, 1.0, 1.0, 0.0),
		doubleArrayOf(-.5, -.5, 0.7, 0.0, 1.0),
		doubleArrayOf(0.5, -.5, 0.1, 1.0, 0.6)
	)
	override val indices = intArrayOf(0, 1, 2)
}