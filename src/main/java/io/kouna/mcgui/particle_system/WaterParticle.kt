package io.kouna.mcgui.particle_system

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import io.kouna.mcgui.utilities.rendering.interpolateTransform
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaterniond
import kotlin.math.pow
import kotlin.random.Random

class WaterParticle(
    val world: World,
    val source: Vector,
    val position: Vector,
    val minSize: Double,
    val maxSize: Double,
    val minSpeed: Double,
    val maxSpeed: Double,
    maxUpAngle: Double = 85.0.toRadians(),
    upAngleBias: Int
) : Particle {
    val color = Color.WHITE.lerpOkLab(Color.fromRGB(0x699cc9), Random.nextDouble().pow(4))

    var gravityAcceleration = .08
    var airDragCoefficient = .02 * 3

    var age = 0
    val maxAge = Random.nextInt(15,25)

    val velocity = FORWARD_VECTOR
    .rotate(
        Quaterniond()
        .rotateY(position.clone().subtract(source).yaw().toDouble())
        .rotateX(-maxUpAngle * (1 - Random.nextDouble().pow(upAngleBias)))
    )
    .multiply(Random.nextDouble(minSpeed, maxSpeed + .000001))

    var rotSpeed = Random.nextDouble(-.2, .2).toFloat()
    var size = Random.nextDouble(minSize, maxSize + .000001).toFloat()


    override fun update() {
        age += 1
        if (age > maxAge) {
            particles.remove(this)
            return
        }

        // accelerate due to gravity
        velocity.y -= gravityAcceleration

        // slow down when in water
        if (position.y < source.y && velocity.y < 0) {
            velocity.multiply(.2)
            age += 3
        }

        // air drag
        velocity.multiply(1 - airDragCoefficient)

        // move
        val oldPosition = position.clone()
        position.add(velocity)

        val justEnteredWater = oldPosition.y > source.y && position.y <= source.y
        if (justEnteredWater && Random.nextBoolean()) {
            // bounce
            position.y = source.y
            velocity.y = -velocity.y * .5
            velocity.x *= .3
            velocity.z *= .3
        }

        // render
        SharedEntityRenderer.render(this, textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.billboard = Display.Billboard.CENTER
                it.interpolationDuration = 1
                it.teleportDuration = 1
//                it.brightness = Brightness(15, 15)
            },
            update = {
                val transform = Matrix4f()
                    .rotateZ(age * rotSpeed)
                    .scale(size)
                    .translate(-.5f, -.5f, 0f)
                    .mul(textBackgroundTransform)

                it.interpolateTransform(transform)


                it.backgroundColor = color.setAlpha((color.alpha * (1 - age.toDouble() / maxAge)).toInt().coerceIn(0, 255))
            }
        ))
    }
}