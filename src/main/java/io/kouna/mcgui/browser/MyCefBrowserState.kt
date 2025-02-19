package io.kouna.mcgui.browser

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.PlanePointDetector
import io.kouna.mcgui.utilities.getFloat
import io.kouna.mcgui.utilities.getQuaternion
import io.kouna.mcgui.utilities.rendering.RenderEntity
import io.kouna.mcgui.utilities.rendering.RenderEntityGroup
import io.kouna.mcgui.utilities.rendering.interpolateTransform
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.cef.browser.CefBrowser
import org.cef.handler.CefRequestHandler
import org.joml.Matrix4f
import org.joml.Quaternionf
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.nio.ByteBuffer

class RenderInfo constructor(
    val world: World,
    val position: Vector,
    val quaternion: Quaternionf,
    val renderHeight: Float = 2f,
    val players: List<Player>
) {
    companion object Factory {
        fun fromEntity(entity: Entity): RenderInfo {
            return RenderInfo(
                world = entity.world,
                position = entity.location.toVector(),
                quaternion = entity.location.getQuaternion(),
                renderHeight = entity.persistentDataContainer.getFloat(NamespacedKey.fromString("browser_app:display_height")!!) ?: 2f,
                players = entity.world.players
            )
        }
    }
}

class MyCefBrowserState(public var size: Dimension) {
    var scroll = Point(0, 0);
    var cursorType: Int = 0;
    var cursorPos: Pair<Int, Int>? = null
    var browser: CefBrowser? = null;
    var group: RenderEntityGroup = RenderEntityGroup()
    var pointDetector: PlanePointDetector? = null
    var entity: Entity? = null
    var onFrameUpdate: ((RenderEntityGroup) -> Unit)? = null
    var onHover: ((Player, Pair<Int, Int>) -> Unit)? = null
    var cefRequestHandler: CefRequestHandler? = null

    fun render(
        info: RenderInfo,
        cursor: Pair<Int, Int>?,

        colorAt: (Pair<Int, Int>) -> Int,
        iterator: ((Pair<Int, Int>) -> Unit) -> Unit,
        onHover: (Player, Pair<Int, Int>) -> Unit
    ) {
        if (pointDetector == null) {
            pointDetector = PlanePointDetector(info.players, info.position)
        }

        val renderWidth = info.renderHeight * size.width / size.height
        val heightPerStep = info.renderHeight / size.height
        val widthPerStep = renderWidth / size.width

        val cursorSize = .05f
        fun Matrix4f.translatePixel(pixel: Pair<Int, Int>): Matrix4f {
            val (x, y) = pixel
            return translate(x * widthPerStep - renderWidth / 2, y * heightPerStep, 0f)
        }

        iterator { pixel ->
            val transform = Matrix4f()
                .rotate(info.quaternion)
                .translatePixel(pixel)
                .scale(widthPerStep, heightPerStep, 1f)

            pointDetector?.lookingAt(transform)?.forEach { player ->
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

            if (!group.items.containsKey(pixel)) {
                group.add(pixel, textRenderEntity(
                    world = info.world,
                    position = info.position,
                    init = {
                        it.text = " "
                        it.brightness = Display.Brightness(15, 15)
                        it.interpolationDuration = 1
                    },
                    update = {
                        it.backgroundColor = Color.fromARGB(colorAt(pixel))
                        it.interpolateTransform(
                            Matrix4f(transform).mul(textBackgroundTransform))
                    }
                ))
            }
        }

        if (cursor != null) {
            val transform = Matrix4f()
                .rotate(info.quaternion)
                .translate(widthPerStep / 2, heightPerStep / 2, 0f)
                .translatePixel(cursor)
                .scale(cursorSize, cursorSize, 1f)
                .rotateZ(Math.PI.toFloat() / 4)
                .translate(-.5f, -.5f, .001f)

            group.add("cursor", textRenderEntity(
                world = info.world,
                position = info.position,
                init = {
                    it.text = " "
                    it.brightness = Display.Brightness(15, 15)
                    it.interpolationDuration = 1
                },
                update = {
                    it.backgroundColor = Color.fromARGB(colorAt(cursor))
                    it.interpolateTransform(
                        Matrix4f(transform).mul(textBackgroundTransform))
                }
            ))
        }

        if (onFrameUpdate != null) {
            onFrameUpdate!!(group)
        }
    }

    fun doPaint(dirtyRectangles: Array<out Rectangle>, buffer: ByteBuffer?, width: Int, height: Int) {
        if (width != size.width || height != size.height) {
            group.clear()
            size.height = height
            size.width = width
        }

        if (entity == null) {
            return
        }

        if (buffer == null) {
            return
        }

        render(
            info = RenderInfo.fromEntity(entity!!),
            cursor = cursorPos,
            onHover = onHover ?: { _, pixel -> cursorPos = pixel },
            iterator = { onPixel ->
                for (ix in 0.. width) {
                    for (iy in 0 .. height) {
                        onPixel(Pair(ix, iy))
                    }
                }
            },
            colorAt = { pixel ->
                val x = pixel.first;
                val y = pixel.second;
                val i = width * x + y
                val b = buffer[i * 4].toInt() and 0xFF
                val g = buffer[i * 4 + 1].toInt() and 0xFF
                val r = buffer[i * 4 + 2].toInt() and 0xFF
                val a = buffer[i * 4 + 3].toInt() and 0xFF
                (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        )
    }
}