package io.kouna.mcgui.paint_program

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.*
import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.random.Random


fun setupPaintProgram() {
    // initialize color picker
    ColorPicker

    // state
    var bitmap = Grid(16, 16) { Color.WHITE }
    var hovered: Pair<Int, Int>? = null

    // custom items
    val brush = CustomItemComponent("paint_app_brush")
    customItemRegistry += createNamedItem(Material.BRUSH, "Paint Brush").attach(brush)
    brush.onInteractEntity { player, _, _ ->
        val pixel = hovered ?: return@onInteractEntity

        bitmap[pixel] = ColorPicker.selected
        io.kouna.mcgui.utilities.playSound(
            player.location,
            Sound.ITEM_BRUSH_BRUSHING_GENERIC,
            1.0f,
            2.0f - Random.nextFloat() * .2f
        )
    }

    val fill = CustomItemComponent("paint_app_fill")
    customItemRegistry += createNamedItem(Material.MILK_BUCKET, "Fill").attach(fill)
    fill.onInteractEntity { player, _, _ ->
        val pixel = hovered ?: return@onInteractEntity

        bitmap.floodFill(pixel, ColorPicker.selected)
        io.kouna.mcgui.utilities.playSound(player.location, Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f)
    }

    val eyeDropper = CustomItemComponent("paint_app_eye_dropper")
    customItemRegistry += createNamedItem(Material.SPIDER_EYE, "Eye Dropper").attach(eyeDropper)
    eyeDropper.onInteractEntity { player, _, _ ->
        val pixel = hovered ?: return@onInteractEntity

        val color = bitmap[pixel]
        ColorPicker.selected = color

        io.kouna.mcgui.utilities.playSound(player.location, Sound.ENTITY_ARMADILLO_BRUSH, 1.0f, 1.0f)
    }

    onTick {
        hovered = null

        val entities = EntityTag("paint_app.canvas").getEntities().take(1)
        for (entity in entities) {
            // resize bitmap if necessary
            val width = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("paint_app:bitmap_width")!!) ?: bitmap.width
            val height = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("paint_app:bitmap_height")!!) ?: bitmap.height

            if (width != bitmap.width || height != bitmap.height) {
                bitmap = Grid(width.toInt(), height.toInt()) { Color.WHITE }
            }

            // render
            SharedEntityRenderer.render(entity to ::setupPaintProgram, renderCanvas(
                world = entity.world,
                position = entity.location.toVector(),
                quaternion = entity.location.getQuaternion(),

                bitmap = bitmap,
                height = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("paint_app:display_height")!!) ?: 2f,

                players = entity.world.players,
                onHover = { _, pixel ->
                    hovered = pixel
                }
            ))
        }
    }
}


@Suppress("SameParameterValue")
private fun renderCanvas(
    world: World,
    position: Vector,
    quaternion: Quaternionf,

    bitmap: Grid<Color>,
    height: Float,

    players: List<Player>,
    onHover: (Player, Pair<Int, Int>) -> Unit
): RenderEntityGroup {
    val group = RenderEntityGroup()
    val pointDetector = PlanePointDetector(players, position)

    val width = height * bitmap.width / bitmap.height
    val heightPerStep = height / bitmap.height
    val widthPerStep = width / bitmap.width

    val cursorSize = .05f

    var hovered: Pair<Int, Int>? = null

    fun Matrix4f.translatePixel(pixel: Pair<Int, Int>): Matrix4f {
        val (x, y) = pixel
        return translate(x * widthPerStep - width / 2, y * heightPerStep, 0f)
    }

    for (pixel in bitmap.indices()) {
        val transform = Matrix4f()
            .rotate(quaternion)
            .translatePixel(pixel)
            .scale(widthPerStep, heightPerStep, 1f)

        pointDetector.lookingAt(transform).forEach { player ->
            hovered = pixel
            onHover(player, pixel)
            group.add(player, RenderEntity(
                clazz = Interaction::class.java,
                location = player.location,
                init = {
                    it.interactionWidth = player.width.toFloat()
                    it.interactionHeight = player.height.toFloat()
                }
            ))
        }

        group.add(pixel, textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.brightness = Display.Brightness(15, 15)
                it.interpolationDuration = 1
            },
            update = {
                it.backgroundColor = bitmap[pixel]
                it.interpolateTransform(
                    Matrix4f(transform).mul(textBackgroundTransform))
            }
        ))
    }


    val cursor = hovered

    if (cursor != null) {
        val transform = Matrix4f()
            .rotate(quaternion)
            .translate(widthPerStep / 2, heightPerStep / 2, 0f)
            .translatePixel(cursor)
            .scale(cursorSize, cursorSize, 1f)
            .rotateZ(Math.PI.toFloat() / 4)
            .translate(-.5f, -.5f, .001f)

        group.add("cursor", textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.brightness = Display.Brightness(15, 15)
                it.interpolationDuration = 1
            },
            update = {
                it.backgroundColor = bitmap[cursor].invert()
                it.interpolateTransform(
                    Matrix4f(transform).mul(textBackgroundTransform))
            }
        ))
    }

    return group
}


private fun Color.invert(): Color {
    return Color.fromRGB(255 - red, 255 - green, 255 - blue)
}

private fun Grid<Color>.floodFill(pixel: Pair<Int, Int>, color: Color) {
    val targetColor = this[pixel]

    val queue = mutableListOf(pixel)
    val visited = mutableSetOf(pixel)

    while (queue.isNotEmpty()) {
        val current = queue.removeAt(0)
        this[current] = color

        for (neighbor in current.neighbors()) {
            if (neighbor in this && neighbor !in visited && this[neighbor] == targetColor) {
                queue.add(neighbor)
                visited.add(neighbor)
            }
        }
    }
}

private fun Pair<Int, Int>.neighbors(): List<Pair<Int, Int>> {
    val (x, y) = this
    return listOf(
        x - 1 to y,
        x + 1 to y,
        x to y - 1,
        x to y + 1
    )
}