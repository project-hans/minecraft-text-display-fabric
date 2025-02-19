package io.kouna.mcgui

import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import io.kouna.mcgui.utilities.rendering.blockRenderEntity
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Color
import org.bukkit.NamespacedKey
import kotlin.math.floor

fun setupColoredCauldrons() {
    val tag = EntityTag("colored_cauldron")

    tag.onTick { marker ->
        val targetColor =
            marker.persistentDataContainer.getColor(NamespacedKey.fromString("colored_cauldron:background")!!) ?:
            Color.fromARGB(0xaa76a4cf.toInt())

        marker.location.block.setType(org.bukkit.Material.BARRIER, false)

        val floored = marker.location.clone().apply {
            x =  floor(x)
            y =  floor(y)
            z =  floor(z)
            pitch = 0f
            yaw = 0f
        }

        SharedEntityRenderer.render(marker, textRenderEntity(
            location = floored.clone().apply {
                y =  floor(y) + (14 / 16.0)
                z =  floor(z) + 1
                pitch = -90f
            },
            init = {
                it.text = " "
                it.interpolationDuration = 1
                it.teleportDuration = 1
                it.setTransformationMatrix(textBackgroundTransform)
            },
            update = {
                val newColor = (it.backgroundColor ?: targetColor).lerpOkLab(targetColor, .2)
                it.backgroundColor = newColor
            }
        ))

        SharedEntityRenderer.render(marker to "cauldron", blockRenderEntity(
            location = floored.clone(),
            init = {
                it.block = org.bukkit.Material.CAULDRON.createBlockData()
            }
        ))
    }

    tag.onInteract { event ->
        val item = event.player.inventory.getItem(event.hand)
        val entity = event.rightClicked

        val addColor = when (item?.type) {
            org.bukkit.Material.RED_DYE -> Color.fromARGB(0xaaFF0000L.toInt())
            org.bukkit.Material.GREEN_DYE -> Color.fromARGB(0xcc00FF00L.toInt())
            org.bukkit.Material.BLUE_DYE -> Color.fromARGB(0xaa0000FFL.toInt())

            // clear
            org.bukkit.Material.LAPIS_LAZULI -> {
                entity.persistentDataContainer.remove(NamespacedKey.fromString("colored_cauldron:background")!!)
                io.kouna.mcgui.utilities.playSound(entity.location, org.bukkit.Sound.ITEM_BOTTLE_EMPTY, 1f, 0f)
                return@onInteract
            }
            else -> return@onInteract
        }

        val oldColor = entity.persistentDataContainer.getColor(NamespacedKey.fromString("colored_cauldron:background")!!)
        val newColor = oldColor?.lerpOkLab(addColor, .5) ?: addColor

        entity.persistentDataContainer.setColor(NamespacedKey.fromString("colored_cauldron:background")!!, newColor)
        io.kouna.mcgui.utilities.playSound(entity.location, org.bukkit.Sound.ITEM_BOTTLE_EMPTY, 1f, .8f)
    }
}