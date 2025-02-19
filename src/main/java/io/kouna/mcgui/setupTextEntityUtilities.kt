package io.kouna.mcgui

import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.math.sin

fun setupTextEntityUtilities() {
    // Rainbow cycle
    var hue = 0
    onTick {
        val entities = EntityTag("rainbow_cycle_animation").getEntities()

        if (entities.isEmpty()) {
            hue = 0
            return@onTick
        }

        hue += 360 / (20 * 3)
        hue %= 360

        val color = hsv(hue.toDouble(), 1.0, 1.0)
        for (entity in entities) {
            (entity as? TextDisplay)?.backgroundColor = color
        }
    }

    // Pulsating animation
    val minOpacity = 255 * (1.0/4)
    val maxOpacity = 255 * (2.0/4)
    val period = 5
    var currentTime = 0
    onTick {
        val entities = EntityTag("pulsating_animation").getEntities().filterIsInstance<TextDisplay>()

        if (entities.isEmpty()) {
            currentTime = 0
            return@onTick
        }

        currentTime++

        val opacity = minOpacity + (maxOpacity - minOpacity) * (sin(currentTime.toDouble() / period) + 1) / 2
        for (entity in entities) {
            val color = Color.fromARGB(0x0000CCCC)
            entity.backgroundColor = color.setAlpha(opacity.toInt())
        }
    }


    // Screen overlay
    val transforms = listOf(
        Quaternionf(),
        Quaternionf().rotateY(Math.PI.toFloat() / 2 * 1),
        Quaternionf().rotateY(Math.PI.toFloat() / 2 * 2),
        Quaternionf().rotateY(Math.PI.toFloat() / 2 * 3),

        Quaternionf().rotateX(Math.PI.toFloat() / 2),
        Quaternionf().rotateX(-Math.PI.toFloat() / 2),
    ).map {
        val size = 2.5f
        Matrix4f()
            .rotate(it)
            .scale(size,size,1f)
            .translate(-.5f, -.5f, -size / 2)
            .mul(textBackgroundTransform)
    }

    onTick {
        for (player in Bukkit.getServer().onlinePlayers) {
            val tag = player.scoreboardTags.firstOrNull { it.startsWith("screen_overlay.") } ?: continue
            player.removeScoreboardTag(tag)

            val colorCode = tag.substringAfterLast(".")

            player.scoreboardTags.removeIf { it.startsWith("screen_overlay_active.") }
            if (colorCode != "clear") player.scoreboardTags.add("screen_overlay_active.$colorCode")
        }

        for (player in Bukkit.getServer().onlinePlayers) {
            val tag = player.scoreboardTags.firstOrNull { it.startsWith("screen_overlay_active.") } ?: continue
            val colorCode = tag.substringAfterLast(".")
            val color = try {
                Color.fromARGB(colorCode.toLong(radix = 16).toInt())
            } catch (e: NumberFormatException) {
                continue
            } catch (e: IllegalArgumentException) {
                continue
            }

            for ((i,transform) in transforms.withIndex()) SharedEntityRenderer.render("screen_overlay" to player to i, textRenderEntity(
                world = player.world,
                position = player.eyeLocation.toVector(),
                init = {
                    it.text = " "
                    it.brightness = Display.Brightness(15, 15)
                    it.teleportDuration = 1
                    it.setTransformationMatrix(transform)
                },
                update = {
                    it.backgroundColor = color

                    // TODO: Remove when this bug is resolved
                    // https://bugs.mojang.com/browse/MC-259812
                    it.isSeeThrough = true
                }
            ))
        }
    }

    // Background color utilities
    onTick {
        for (entity in Bukkit.getServer().worlds.flatMap { it.entities }.filterIsInstance<TextDisplay>()) {
            val newColor = entity.persistentDataContainer.getColor(NamespacedKey.fromString("text_utilities:background")!!) ?: continue
            val lerpSpeed = entity.persistentDataContainer.getDouble(NamespacedKey.fromString("text_utilities:background_lerp_speed")!!) ?: 1.0

            val oldColor = entity.backgroundColor ?: newColor
            entity.backgroundColor = oldColor.lerpRGB(newColor, lerpSpeed)
        }
    }
}