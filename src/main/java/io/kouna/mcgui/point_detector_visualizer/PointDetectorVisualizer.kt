package io.kouna.mcgui.point_detector_visualizer

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import io.kouna.mcgui.utilities.rendering.blockRenderEntity
import io.kouna.mcgui.utilities.rendering.interpolateTransform
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf

fun setUpPointDetectorVisualizer() {
    val item = CustomItemComponent("point_detector_visualizer")
    customItemRegistry += createNamedItem(Material.BLAZE_ROD, "Next").apply {  item.attach(this) }


    var plane: Triple<World, Vector, Matrix4f>? = null
    var line: Pair<Vector, Vector>? = null
    var inverted = false
    var invertedTime = 0


    item.onGestureUse { player, _ ->


        // reset
        if (inverted) {
            io.kouna.mcgui.utilities.playSound(player.location, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 1.5f)
            plane = null
            line = null
            inverted = false
            return@onGestureUse
        }

        io.kouna.mcgui.utilities.playSound(player.location, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)

        // spawn plane
        if (plane == null) {
            val offset = Vector(1.1,1.1,.0)
            plane = Triple(
                player.world,
                player.eyeLocation.toVector().add(player.eyeLocation.direction.multiply(3)).subtract(offset),
                Matrix4f()
                    .translate(offset.toVector3f())
                    .scale(.5f,.4f,.3f)
                    .rotateYXZ(.5f, .4f, .0f)
                    .scale(2f,1f,1f)
                    .rotateZ(45f.toRadians())
            )
            return@onGestureUse
        }

        // spawn line
        if (line == null) {
            line = Pair(
                player.eyeLocation.toVector().add(player.eyeLocation.direction.multiply(.1)),
                player.eyeLocation.toVector().add(player.eyeLocation.direction.multiply(10)),
            )
            return@onGestureUse
        }

        if (!inverted) {
            inverted = true
            invertedTime = 0
            return@onGestureUse
        }
    }

    onTick {
        val plane = plane ?: return@onTick
        val world = plane.first

        if (inverted) invertedTime += 1
        else invertedTime = 0

        val lerpAmount = (invertedTime / 15.0).coerceIn(0.0, 1.0)
        val eased = lerpAmount * lerpAmount * (3 - 2 * lerpAmount)
        val invertMatrix = Matrix4f().lerp(Matrix4f(plane.third).invert(), eased.toFloat())

        val planeColor = Color.fromRGB(0x9c0610)
        val invertedColor = Color.fromRGB(0x069c9c).setAlpha((255 * .75).toInt())

        SharedEntityRenderer.render("point_detector_visualizer_plane", textRenderEntity(
            world = world,
            position = plane.second,
            init = {
                it.text = " "
                it.brightness = Display.Brightness(15, 15)
                it.backgroundColor = planeColor
            },
            update = {
                it.setTransformationMatrix(Matrix4f(plane.third).mul(textBackgroundTransform))
            }
        ))


        SharedEntityRenderer.render("point_detector_visualizer_plane_transformed", textRenderEntity(
            world = world,
            position = plane.second,
            init = {
                it.text = " "
                it.brightness = Display.Brightness(15, 15)
                it.teleportDuration = 1
                it.interpolationDuration = 1
            },
            update = {
                it.backgroundColor = invertedColor.setAlpha((invertedColor.alpha * eased).toInt())
                it.interpolateTransform(Matrix4f(invertMatrix).mul(plane.third).mul(textBackgroundTransform))
            }
        ))


        line?.let { line ->
            val point1 = line.first.clone().subtract(plane.second)
            val point2 = line.second.clone().subtract(plane.second)

            SharedEntityRenderer.render("point_detector_visualizer_line", blockRenderEntity(
                world = world,
                position = plane.second,
                init = {
                    it.block = Material.GOLD_BLOCK.createBlockData()
                    it.brightness = Display.Brightness(15, 15)
                    it.teleportDuration = 1
                    it.interpolationDuration = 1
                },
                update = {
                    val thickness = .02f
                    val diff = point2.clone().subtract(point1)
                    val length = diff.length()
                    val rotation = Quaternionf().rotationTo(FORWARD_VECTOR.toVector3f(), diff.toVector3f())
                    it.interpolateTransform(
                        Matrix4f()
                            .mul(invertMatrix)
                            .translate(point1.toVector3f())
                            .rotate(rotation)
                            .scale(thickness, thickness, length.toFloat())
                            .translate(-.5f, -.5f, 0f)

                    )
                }
            ))
        }
    }
}