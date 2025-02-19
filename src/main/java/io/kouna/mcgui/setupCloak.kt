package io.kouna.mcgui

import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.*
import org.bukkit.*
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector4f
import kotlin.random.Random

fun setupCloak() {
    val cloak = CustomItemComponent("cloak")
    customItemRegistry += createNamedItem(Material.GREEN_DYE, "Handheld Cloak").attach(cloak)

    cloak.onHeldTick { player, _ ->
        val alpha = player.scoreboardTags.firstOrNull { it.startsWith("cloak_alpha.") }?.substringAfter(".")?.toIntOrNull() ?: 255
        val noise = player.scoreboardTags.firstOrNull { it.startsWith("cloak_noise.") }?.substringAfter(".")?.toDoubleOrNull() ?: 30.0


        SharedEntityRenderer.render("cloak" to player, cloak(
            world = player.world,
            position = player.eyeLocation.toVector(),
            rotation = player.eyeLocation.getQuaternion(),
            eyePosition = player.eyeLocation.toVector().add(player.eyeLocation.direction.multiply(4.0)),
            alpha = alpha,
            noiseAmount = noise,
        ))

    }
}

fun cloak(
    world: World,
    position: Vector,
    rotation: Quaternionf,
    eyePosition: Vector,
    alpha: Int,
    noiseAmount: Double,
): RenderEntityGroup {

    val xSize = 1f
    val ySize = 1.5f

    val xItems = 16
    val yItems = (xItems * ySize / xSize).toInt()

    val forwardDistance = 1.5f
    val castDistance = 100.0

    val group = RenderEntityGroup()
    for (x in 0 until xItems) {
        for (y in 0 until yItems) {
            val xItemSize = xSize / xItems
            val yItemSize = ySize / yItems

            val transform = Matrix4f()
                .rotate(rotation)
                .translate(-xSize / 2f, -.5f - ySize / 2f, 0f)
                .translate(x * xItemSize, y * yItemSize, forwardDistance)
                .scale(xItemSize, yItemSize, 1f)

            group.add(x to y, textRenderEntity(
                world = world,
                position = position,
                init = {
                    it.text = " "
                    it.interpolationDuration = 1
                    it.teleportDuration = 1
                },
                update = {
                    it.interpolateTransform(Matrix4f(transform).mul(textBackgroundTransform))

                    val relative = transform.transform(Vector4f(.5f,.5f,.0f,1f)).toVector3f()
                    val center = position.clone().add(Vector().copy(relative))
                    val direction = center.subtract(eyePosition).normalize()

                    val hitBlock = world.raycastGround(eyePosition, direction, castDistance)
                    val data = hitBlock?.hitBlock?.blockData

                    val newColor = if (data !== null) {
                        val color = getColorFromBlock(data) ?: it.backgroundColor ?: return@textRenderEntity

                        val positionSeed = hitBlock.hitPosition
                        val seed = (positionSeed.x * 1000 + positionSeed.y * 100 + positionSeed.z * 10).toInt() + x * 10000 + y * 10_0000

                        color.noise(noiseAmount, seed).setAlpha(alpha)
//                        Color.RED.setAlpha(180)
                    } else {
                        val skyBottomColor = Color.fromRGB(0xd6edfa)
                        val skyTopColor = Color.fromRGB(0x5e92d4)
                        val skyBottom = (-20.0).toRadians()
                        val skyTop = 40.0.toRadians()
                        val pitch = direction.pitch()

                        val fraction = ((pitch - skyBottom) / (skyTop - skyBottom)).coerceIn(.0, 1.0)

                        skyBottomColor.lerpOkLab(skyTopColor, fraction).setAlpha(alpha) //.setAlpha(0)
                    }

                    it.backgroundColor = (it.backgroundColor ?: newColor).lerpOkLab(newColor, .3)
                }
            ))

        }
    }

    return group
}


@Suppress("SameParameterValue")
private fun World.raycastGround(position: Vector, direction: Vector, maxDistance: Double): RayTraceResult? {
    return this.rayTraceBlocks(position.toLocation(this), direction, maxDistance, FluidCollisionMode.NEVER, true)
}

private fun Color.noise(amount: Double, seed: Int): Color {
    if (amount == 0.0) return this

    val random = Random(seed)
    val noise = random.nextDouble(-amount, amount)
    return Color.fromARGB(
        alpha,
        (red + noise).coerceIn(0.0, 255.0).toInt(),
        (green + noise).coerceIn(0.0, 255.0).toInt(),
        (blue + noise).coerceIn(0.0, 255.0).toInt(),
    )
}



//SharedEntityRenderer.render("test" to x to y, blockRenderEntity(
//world = player.world,
//position = startCast.toVector(),
//init = {
//    it.brightness = Brightness(15, 15)
//    it.interpolationDuration = 1
//    it.block = Material.REDSTONE_BLOCK.createBlockData()
//},
//update = {
//    it.setTransformationMatrix(Matrix4f()
//        .rotate(Quaternionf().rotateTo(FORWARD_VECTOR.toVector3f(), direction.toVector3f()))
//        .scale(.01f, .01f, 15f)
//        .translate(.5f, .5f, 0f)
//    )
//}
//))