package io.kouna.mcgui.paint_program

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.*
import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.math.absoluteValue

object HuePicker {
    var hue = 0
    var onPick: () -> Unit = {}

    init {
        onTick {
            val entities = EntityTag("paint_app.hue_picker").getEntities().take(1)

            for (entity in entities) SharedEntityRenderer.render(entity to HuePicker, hueSelector(
                world = entity.world,
                position = entity.location.toVector(),
                rotation = entity.location.getQuaternion(),

                selectedHue = hue,

                width = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("paint_app:width")!!) ?: .2f,
                height = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("paint_app:height")!!) ?: 2f,
                items = entity.persistentDataContainer.getInt(NamespacedKey.fromString("paint_app:items")!!) ?: (360 / 3),

                players = entity.world.players,
                onClick = { _, hue ->
                    this.hue = hue
                    onPick()
                    io.kouna.mcgui.utilities.playSound(entity.location, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)
                }
            ))
        }
    }
}


@Suppress("SameParameterValue")
private fun hueSelector(
    world: World,
    position: Vector,
    rotation: Quaternionf,

    width: Float,
    height: Float,
    items: Int,

    selectedHue: Int,

    players: List<Player>,
    onClick: (Player, Int) -> Unit,
): RenderEntityGroup {
    val hoverScale = .3f

    val selectionBorderTranslate = .002f
    val selectionColorTranslate = .003f

    val selectionBorder = .025f
    val selectionHeight = .05f

    var hovered: Double? = null
    fun closeness(hue: Double): Float {
        val range = 30
        val target = hovered ?: return 0f
        val t = (1 - (hue - target).absoluteValue.toFloat() / range).coerceIn(0f, 1f)
        return t * t * (3.0f - 2.0f * t)
    }

    fun Matrix4f.translateHue(hue: Double) = translate(0f, hue.toFloat() * height / 360, 0f)

    val group = RenderEntityGroup()
    val pointDetector = PlanePointDetector(players, position)

    for (i in 0 until items) {
        val hue = i * (360.0 / items)

        fun planeTransform(scale: Float) = Matrix4f()
            .rotate(rotation)
            .translateHue(hue)
            .scale(width * scale, height / items, 1f)
            .translate(-.5f, 0f, 0f)

        pointDetector.detectClick(planeTransform(1f)).forEach { result ->
            hovered = hue
            if (result.isClicked) onClick(result.player, hue.toInt())
        }

        group.add(hue, textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.backgroundColor = hsv(hue, 1.0, 1.0)
                it.brightness = Display.Brightness(15, 15)
                it.interpolationDuration = 1
            },
            update = {
                val closeness = closeness(hue)
                val scaled = 1.0f + closeness * hoverScale
                it.interpolateTransform(planeTransform(scaled).mul(textBackgroundTransform))
            }
        ))
    }

    val closeness = closeness(selectedHue.toDouble())
    val scaled = (1.0f + closeness * hoverScale)

    fun cursorTransform(cursorWidth: Float, cursorHeight: Float, zTranslate: Float) = Matrix4f()
        .rotate(rotation)
        .translateHue(selectedHue.toDouble())
        .translate(0f, height / items / 2, 0f)
        .scale(cursorWidth * scaled, cursorHeight, 1f)
        .translate(-.5f, -.5f, zTranslate)

    group.add("selection_border", textRenderEntity(
        world = world,
        position = position,
        init = {
            it.text = " "
            it.backgroundColor = Color.WHITE
            it.brightness = Display.Brightness(15, 15)
            it.interpolationDuration = 1
        },
        update = {
            val transform = cursorTransform(width + selectionBorder, selectionHeight + selectionBorder, selectionBorderTranslate)
            it.interpolateTransform(transform.mul(textBackgroundTransform))
        }
    ))

    group.add("selection_color", textRenderEntity(
        world = world,
        position = position,
        init = {
            it.text = " "
            it.brightness = Display.Brightness(15, 15)
            it.interpolationDuration = 1
        },
        update = {
            val transform = cursorTransform(width, selectionHeight, selectionColorTranslate)
            it.interpolateTransform(transform.mul(textBackgroundTransform))
            it.backgroundColor = hsv(selectedHue.toDouble(), 1.0, 1.0)
        }
    ))

    return group
}