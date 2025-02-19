package io.kouna.mcgui.particle_system

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.interpolateOkLab
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import io.kouna.mcgui.utilities.rendering.interpolateTransform
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.Display.Brightness
import org.bukkit.util.Vector
import org.joml.Matrix4f
import kotlin.random.Random


class FlameParticle(
    val world: World,
    val position: Vector,
    val palette: List<Pair<Double, Color>>,
): Particle {
    val originalPosition = position.clone()

    var rotSpeed = Random.nextFloat() * .4f - .2f

    var age = 0
    val maxAge = 15//Random.nextInt(10, 20)
    val velocity = Vector(
        Random.nextDouble(-.1, .1),
        Random.nextDouble(-.1, .1),
        Random.nextDouble(-.1, .1)
    ).normalize().multiply(.1)

    override fun update() {
        age++

        if (age > maxAge) {
            particles.remove(this)
            return
        }

        position.add(velocity)

        // rise
        velocity.y += .03

        // pull towards the center
        val center = originalPosition.clone().apply { y = position.y }
        val direction = center.clone().subtract(position).normalize()
        velocity.add(direction.multiply(.02))

        // air drag
        velocity.multiply(.9)

        SharedEntityRenderer.render(this, textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.brightness = Brightness(15, 15)
                it.billboard = Display.Billboard.CENTER
                it.interpolationDuration = 1
                it.teleportDuration = 1
            },
            update = {
                val transform = Matrix4f()
//                    .translate(position.toVector3f().sub(originalPosition.toVector3f()))
                    .rotateZ(age * rotSpeed)
                    .scale(.1f)
                    .translate(-.5f, -.5f, 0f)
                    .mul(textBackgroundTransform)

                it.interpolateTransform(transform)
                it.backgroundColor = palette.interpolateOkLab(age.toDouble() / maxAge)
            }
        ))
    }
}

private val yellow = Color.fromRGB(252, 211, 3)
private val orange = Color.fromRGB(255, 136, 0)
private val red = Color.fromRGB(222, 64, 11)

val blueToOrangeFlamePalette = listOf(
    0.00 to Color.WHITE,
    0.05 to Color.WHITE,
    0.10 to Color.fromRGB(64, 188, 255), // light blue
    0.30 to Color.fromRGB(0, 17, 201), // blue
    0.50 to yellow,
    0.70 to red,
    0.80 to Color.BLACK,
    1.0  to Color.BLACK.setAlpha(0),
)


val orangeFlamePalette = listOf(
    0.00 to Color.WHITE,
    0.05 to Color.WHITE,
    0.20 to yellow,
    0.40 to orange,
    0.70 to red,
    0.80 to Color.BLACK,
    1.0  to Color.BLACK.setAlpha(0),
)


val blackFlamePalette = listOf(
    0.00 to Color.WHITE,
    0.10 to Color.WHITE,
    0.60 to Color.BLACK,
    0.70 to Color.BLACK,
    1.0  to Color.BLACK.setAlpha(0),
)