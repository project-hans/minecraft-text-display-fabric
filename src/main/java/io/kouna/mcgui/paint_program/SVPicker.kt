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

object SVPicker {
    var sv = 1.0 to 1.0
    var onPick: () -> Unit = {}

    init {
        onTick {
            val entities = EntityTag("paint_app.sv_picker").getEntities().take(1)
            for (entity in entities) SharedEntityRenderer.render(entity to SVPicker, svSelector(
                world = entity.world,
                position = entity.location.toVector(),
                quaternion = entity.location.getQuaternion(),

                hue = HuePicker.hue,
                selectedSV = sv,

                width = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("paint_app:width")!!) ?: 2f,
                height = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("paint_app:height")!!) ?: 2f,
                items = entity.persistentDataContainer.getInt(NamespacedKey.fromString("paint_app:items")!!) ?: 50,

                players = entity.world.players,
                onClick = { _, sv ->
                    this.sv = sv
                    onPick()
                    io.kouna.mcgui.utilities.playSound(entity.location, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)
                }
            ))
        }
    }
}

@Suppress("SameParameterValue")
private fun svSelector(
    world: World,
    position: Vector,
    quaternion: Quaternionf,

    width: Float,
    height: Float,
    items: Int,

    hue: Int,
    selectedSV: Pair<Double, Double>,

    players: List<Player>,
    onClick: (Player, Pair<Double, Double>) -> Unit,
): RenderEntityGroup {
    val cursorBorder = .025f
    val cursorSize = .07f

    var hovered: Pair<Double, Double>? = null

    val group = RenderEntityGroup()
    val pointDetector = PlanePointDetector(players, position)

    val widthOffBy1 = width - (width / items)
    val heightOffBy1 = height - (height / items)

    fun Matrix4f.translateSV(sv: Pair<Double, Double>): Matrix4f {
        val (s,v) = sv
        return translate(s.toFloat() * widthOffBy1 - width / 2, v.toFloat() * heightOffBy1, 0f)
    }

    val widthPerItem = widthOffBy1 / (items - 1)
    val heightPerItem = heightOffBy1 / (items - 1)

    fun step() = (0..< items).map { it.toDouble() / (items - 1) }

    for (s in step()) for (v in step()) {
        val sv = s to v

        val transform = Matrix4f()
            .rotate(quaternion)
            .translateSV(sv)
            .scale(widthPerItem, heightPerItem, 1f)

        pointDetector.detectClick(transform).forEach { result ->
            hovered = sv
            if (result.isClicked) onClick(result.player, sv)
        }

        group.add(sv, textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.brightness = Display.Brightness(15, 15)
                it.interpolationDuration = 1
            },
            update = {
                it.backgroundColor = hsv(hue.toDouble(), s, v)
                it.interpolateTransform(
                    Matrix4f(transform).mul(textBackgroundTransform))
            }
        ))
    }


    fun cursor(
        sv: Pair<Double, Double>,
        borderColor: Color,
        extraTranslate: Float
    ): RenderEntityGroup {
        fun transform(size: Float, zTranslate: Float) = Matrix4f()
            .rotate(quaternion)
            .translate(widthPerItem / 2, heightPerItem / 2, 0f)
            .translateSV(sv)
            .scale(size, size, 1f)
            .rotateZ(Math.PI.toFloat() / 4)
            .translate(-.5f, -.5f, zTranslate)


        val transform = transform(cursorSize, .001f + extraTranslate)
        val borderTransform = transform(cursorSize + cursorBorder, extraTranslate)

        val cursorGroup = RenderEntityGroup()

        cursorGroup.add("border", textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.backgroundColor = borderColor
                it.brightness = Display.Brightness(15, 15)
                it.interpolationDuration = 1
            },
            update = {
                it.interpolateTransform(borderTransform.mul(textBackgroundTransform))
            }
        ))

        cursorGroup.add("color", textRenderEntity(
            world = world,
            position = position,
            init = {
                it.text = " "
                it.brightness = Display.Brightness(15, 15)
                it.interpolationDuration = 1
            },
            update = {
                it.backgroundColor = hsv(hue.toDouble(), sv.first, sv.second)
                it.interpolateTransform(Matrix4f(transform).mul(textBackgroundTransform))
            }
        ))

        return cursorGroup
    }

    group.add("selection", cursor(
        sv = selectedSV,
        borderColor = Color.WHITE,
        extraTranslate = 0.002f
    ))

    val cursor = hovered
    if (cursor != null) group.add("cursor", cursor(
        sv = cursor,
        borderColor = Color.BLACK,
        extraTranslate = 0.001f
    ))

    return group
}
