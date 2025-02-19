package io.kouna.mcgui.particle_system

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaterniond
import org.joml.Quaternionf
import kotlin.math.sin
import kotlin.random.Random

class FireFlyParticle(
    val world: World,
    val position: Vector
): Particle {
    val spawnPosition = position.clone()

    var age = 0
    val maxAge = 20

    val orientation = Quaternionf()

    var opacity = 0f
    val fadeInTicks = 10
    var fadeOutTicks = 10

    val velocity = Vector()

    val color = Color.YELLOW.lerpRGB(Color.WHITE, .5)
    val blinkColor = Color.BLACK

    var blinkTick = 0

    fun keepAlive() {
        age = 0
    }

    override fun update() {
        age++
        blinkTick--

        if (age > maxAge) {
            opacity = opacity.moveTowards(0f, 255f / fadeOutTicks)
            if (opacity <= .0) {
                particles.remove(this)
            }
            return
        }

        opacity = opacity.moveTowards(255f, 255f / fadeInTicks)


        val blinkDuration = 20
        if (blinkTick <= 0) blinkTick = blinkDuration + Random.nextInt(20,100)

        val blink = if (blinkTick in 0..<blinkDuration) {
            sin(blinkTick.toDouble() / blinkDuration * Math.PI)
        } else {
            .0
        }

        val rand = .6
        orientation.rotateY(Random.nextDouble(-rand, rand).toFloat())
        orientation.rotateX(Random.nextDouble(-rand, rand).toFloat())

        // rotate towards spawn position
        val direction = spawnPosition.clone().subtract(position)
        if (!direction.isZero) {
            direction.normalize()
            val targetOrientation = Quaternionf().rotationTo(FORWARD_VECTOR.toVector3f(), direction.toVector3f())
            orientation.slerp(targetOrientation, .03f)
        }

        val acc = FORWARD_VECTOR.rotate(Quaterniond(orientation)).multiply(Random.nextDouble(.0, .02))

        // move
        velocity.add(acc)
        velocity.multiply(.9)

        position.add(velocity)

        SharedEntityRenderer.render(this, textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.billboard = Display.Billboard.CENTER
                it.teleportDuration = 1
                it.setTransformationMatrix(Matrix4f().scale(.1f).mul(textBackgroundTransform))
            },
            update = {
                it.brightness = Display.Brightness((15 * (1 - blink)).toInt(), 15)
                it.backgroundColor = color.setAlpha(opacity.toInt()).lerpRGB(blinkColor, blink)
            }
        ))
    }


}