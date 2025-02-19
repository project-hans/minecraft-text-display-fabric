package io.kouna.mcgui.bitmaps.scenes

import io.kouna.mcgui.bitmaps.FragmentData
import io.kouna.mcgui.bitmaps.RenderBuffer
import io.kouna.mcgui.bitmaps.drawTriangles
import io.kouna.mcgui.bitmaps.meshes.Cube
import io.kouna.mcgui.bitmaps.meshes.Water
import io.kouna.mcgui.bitmaps.shaders.RGBShader
import io.kouna.mcgui.bitmaps.shaders.WaterShader
import io.kouna.mcgui.utilities.FORWARD_VECTOR
import io.kouna.mcgui.utilities.toRadians
import org.bukkit.Color
import org.joml.Matrix4d
import org.joml.Quaterniond
import org.joml.Vector3d

private val TRANSPARENT = Color.fromARGB(0, 0, 0, 0)

class RotatingCubeScene : Scene {
    // time
    var tick = 0

    // cube
    var cubeOrientation = Quaterniond()
    var rotateCube = true
    var cubeRotationPerTick = Quaterniond().apply {
        val amount = .3 * (2.0 * Math.PI) / 20.0
        rotateY(amount)
        rotateX(amount/4)
    }

    // buffer
    val buffer = RenderBuffer(64 * 2, 64) { FragmentData(TRANSPARENT) }
    override fun getBitmap() = buffer.map { it.color }

    // camera
    var cameraAngleX = 0.0
    var cameraAngleY = 30.0
    var cameraDistance = 15.0
    private var camera = Camera(Math.PI / 4, aspectRatio = buffer.width.toDouble() / buffer.height.toDouble())
    private var reflectionCamera = Camera(Math.PI / 2.2).apply { lookAt(
        Vector3d(0.0, -5.0, 0.0),  // camera position (at water surface)
        Vector3d(0.0, 0.0, 0.0),  // cube position
        Vector3d(0.0, 0.0, -1.0) // up vector
    ) }

    // water
    val reflectionTexture = RenderBuffer(64, 64) { FragmentData(CLEAR_COLOR) }
    var drawWater = true


    override fun update() {
        tick++

        // clear buffer
        buffer.setAll { FragmentData(CLEAR_COLOR) }

        // update camera position
        val cameraRotation = Quaterniond().rotationYXZ(cameraAngleY.toRadians(), cameraAngleX.toRadians(), 0.0)
        val cameraPosition = FORWARD_VECTOR.toVector3d().mul(cameraDistance).rotate(cameraRotation)
        camera.lookAt(cameraPosition, Vector3d(), Vector3d(0.0, 1.0, 0.0))

        // rotate cube
        if (rotateCube) cubeOrientation.premul(cubeRotationPerTick)

        // get cube transform
        val cubeTransform = Matrix4d().rotate(cubeOrientation)

        // draw cube
        RGBShader.transform = camera.getTransform(cubeTransform)
        buffer.drawTriangles(Cube, RGBShader, true)

        if (drawWater) {
            // render reflection
            reflectionTexture.setAll { FragmentData(TRANSPARENT) }
            RGBShader.transform = reflectionCamera.getTransform(cubeTransform)
            reflectionTexture.drawTriangles(Cube, RGBShader, true)

            // draw water
            WaterShader.reflectionTexture = reflectionTexture
            WaterShader.transform = camera.getTransform()
            WaterShader.distortionFrame = tick / 20.0
            buffer.drawTriangles(Water, WaterShader, true)
        }
    }
}

private class Camera(foxy: Double, aspectRatio: Double = 1.0) {
    val view = Matrix4d()
    val projection = Matrix4d().perspective(foxy, aspectRatio, 0.1, 1000.0)

    fun getTransform(model: Matrix4d = Matrix4d()): Matrix4d {
        return Matrix4d(projection).mul(view).mul(model)
    }

    fun lookAt(position: Vector3d, target: Vector3d, up: Vector3d) {
        this.view.setLookAt(position, target, up)
    }
}