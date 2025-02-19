package io.kouna.mcgui.bitmaps

import io.kouna.mcgui.bitmaps.scenes.MandelbrotSetScene
import io.kouna.mcgui.bitmaps.scenes.RainbowTriangleScene
import io.kouna.mcgui.bitmaps.scenes.RotatingCubeScene
import io.kouna.mcgui.bitmaps.scenes.Scene
import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.*
import io.kouna.mcgui.utilities.rendering.RenderEntityGroup
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import io.kouna.mcgui.utilities.rendering.interpolateTransform
import io.kouna.mcgui.utilities.rendering.textRenderEntity
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.*

@Suppress("UnstableApiUsage")
fun setupBitmapDisplay() {
    var bitmapScene: Scene = MandelbrotSetScene()

    // Player lock system
    fun Player.lockMovement() {
        if (vehicle != null) {
            if (vehicle?.scoreboardTags?.contains("player_lock") == true) {
                vehicle?.scoreboardTags?.add("player_lock_active")
            }
            return
        }

        world.spawn(eyeLocation.add(.0,-1.02,.0), org.bukkit.entity.ArmorStand::class.java) {
            it.setGravity(false)
            it.isInvisible = true
            it.isInvulnerable = true
            it.isSilent = true
            it.isCollidable = false
            it.isMarker = true
            it.scoreboardTags.add("player_lock")
            it.scoreboardTags.add("player_lock_active")
            it.addPassenger(this)
        }
    }

    onTick {
        for (entity in EntityTag("player_lock").getEntities()) {
            if (!entity.scoreboardTags.contains("player_lock_active") || entity.passengers.isEmpty()) {
                entity.remove()
                continue
            }

            entity.scoreboardTags.remove("player_lock_active")
        }
    }


    // Change scene item
    val changeScene = CustomItemComponent("change_scene")
    customItemRegistry += createNamedItem(org.bukkit.Material.CLOCK, "Change Scene").attach(changeScene)

    changeScene.onGestureUse { player, _ ->
        bitmapScene = when (bitmapScene) {
            is RainbowTriangleScene -> RotatingCubeScene()
            is RotatingCubeScene -> MandelbrotSetScene()
            else -> RainbowTriangleScene()
        }

        io.kouna.mcgui.utilities.playSound(player.location, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)
    }

    // Move 3D renderer camera
    val cameraMoveSpeed = .25
    val cameraRotateSpeed = 2

    fun normalizeCamera() {
        val rotatingCube = bitmapScene as? RotatingCubeScene ?: return
        if (rotatingCube.cameraAngleY > 360) rotatingCube.cameraAngleY -= 360.0
        rotatingCube.cameraAngleX = rotatingCube.cameraAngleX.coerceIn(-90.0, 90.0)
        rotatingCube.cameraDistance = rotatingCube.cameraDistance.coerceIn(3.0, 100.0)
    }

    // Orbit Camera
    val orbitCamera = CustomItemComponent("orbit_camera")
    customItemRegistry += createNamedItem(org.bukkit.Material.BLAZE_ROD, "Orbit Camera").attach(orbitCamera)
    orbitCamera.onHeldTick { player, _ ->
        val rotatingCube = bitmapScene as? RotatingCubeScene ?: return@onHeldTick
        player.lockMovement()

        val input = player.currentInput
        if (input.isLeft)     rotatingCube.cameraAngleY -= cameraRotateSpeed
        if (input.isRight)    rotatingCube.cameraAngleY += cameraRotateSpeed
        if (input.isForward)  rotatingCube.cameraAngleX -= cameraRotateSpeed
        if (input.isBackward) rotatingCube.cameraAngleX += cameraRotateSpeed
        normalizeCamera()
    }

    // Move Camera
    val moveCamera = CustomItemComponent("move_camera")
    customItemRegistry += createNamedItem(org.bukkit.Material.BREEZE_ROD, "Move Camera").attach(moveCamera)
    moveCamera.onHeldTick { player, _ ->
        val rotatingCube = bitmapScene as? RotatingCubeScene ?: return@onHeldTick
        player.lockMovement()

        val input = player.currentInput
        if (input.isLeft)     rotatingCube.cameraAngleY -= cameraRotateSpeed
        if (input.isRight)    rotatingCube.cameraAngleY += cameraRotateSpeed
        if (input.isForward)  rotatingCube.cameraDistance -= cameraMoveSpeed
        if (input.isBackward) rotatingCube.cameraDistance += cameraMoveSpeed
        normalizeCamera()
    }

    // Mandelbrot Zoom
    var cursor: Pair<Int, Int>? = null
    var worldSpaceCursor: Vector2d? = null
    var moveTowardsCursor = false
    onTick {
        worldSpaceCursor = null
        moveTowardsCursor = false
    }

    fun handleCursor(player: Player) {
        val mandelbrotScene = bitmapScene as? MandelbrotSetScene ?: return
        val (x,y) = cursor ?:return

        val bitmap = bitmapScene.getBitmap()
        val wsCursor = Vector2d(
            mandelbrotScene.viewRect.minX + mandelbrotScene.viewRect.width * (x.toDouble() / bitmap.width),
            mandelbrotScene.viewRect.minY + mandelbrotScene.viewRect.height * (y.toDouble() / bitmap.height)
        )
        worldSpaceCursor = wsCursor

        val input = player.currentInput
        if (input.isForward)  mandelbrotScene.viewRectAnchor.zoom(wsCursor, 1 - .05)
        if (input.isBackward) mandelbrotScene.viewRectAnchor.zoom(wsCursor, 1 + .05)

        player.sendActionBar(String.format("maxIterations: %d | zoomLevel: %.2f", mandelbrotScene.maxIterations, mandelbrotScene.zoomLevel))
    }

    val mandelbrotZoom = CustomItemComponent("mandelbrot_zoom")
    customItemRegistry += createNamedItem(org.bukkit.Material.SPECTRAL_ARROW, "Mandelbrot Zoom").attach(mandelbrotZoom)
    mandelbrotZoom.onHeldTick { player, _ ->
        player.lockMovement()
        moveTowardsCursor = true
        handleCursor(player)
    }

    val mandelbrotZoomVar = CustomItemComponent("mandelbrot_zoom_var")
    customItemRegistry += createNamedItem(org.bukkit.Material.ARROW, "Mandelbrot Zoom").attach(mandelbrotZoomVar)
    mandelbrotZoomVar.onHeldTick { player, _ ->
        player.lockMovement()
        handleCursor(player)
    }

    onTick {
        val mandelbrotScene = bitmapScene as? MandelbrotSetScene ?: return@onTick
        mandelbrotScene.viewRect.lerp(mandelbrotScene.viewRectAnchor, .4)

        if (!moveTowardsCursor) return@onTick

        val newRect = Rect.fromCenter(worldSpaceCursor ?: return@onTick, mandelbrotScene.viewRectAnchor.dimensions)
        mandelbrotScene.viewRect.lerp(newRect, .15)
    }

    // Display
    onTick {
        val entities = EntityTag("bitmap_display").getEntities().take(1)

        if (entities.isNotEmpty()) {
            bitmapScene.update()
        }

        // modify scene settings

        val currentCursor = cursor
        cursor = null
        for (entity in entities) {
            (bitmapScene as? MandelbrotSetScene)?.let { mandelbrotScene ->
                entity.persistentDataContainer.getInt(NamespacedKey.fromString("bitmap_display:max_iterations_base")!!)?.let {
                    mandelbrotScene.maxIterationsBase = it
                }
                entity.persistentDataContainer.getDouble(NamespacedKey.fromString("bitmap_display:max_iterations_scale")!!)?.let {
                    mandelbrotScene.maxIterationsScale = it
                }
            }

            SharedEntityRenderer.render("bitmap" to entity, bitmapToRenderEntities(
                world = entity.world,
                position = entity.location.toVector(),
                quaternion = entity.location.getQuaternion(),
                bitmap = bitmapScene.getBitmap(),
                cursor = if (worldSpaceCursor != null) currentCursor else null,
                players = entity.world.players,
                onHover = { _, newCursor ->
                    cursor = newCursor
                }
            ))
        }
    }
}

private fun Rect.zoom(point: Vector2d, scale: Double): Rect {
    val fraction = 1.0 - scale
    minX = minX.lerp(point.x, fraction)
    maxX = maxX.lerp(point.x, fraction)
    minY = minY.lerp(point.y, fraction)
    maxY = maxY.lerp(point.y, fraction)
    return this
}


private fun bitmapToRenderEntities(
    world: World,
    position: Vector,
    quaternion: Quaternionf,
    players: List<Player>,
    bitmap: Grid<Color>,
    cursor: Pair<Int, Int>?,
    onHover: (Player, Pair<Int, Int>) -> Unit = { _, _ -> },
): RenderEntityGroup {
    val pointDetector = PlanePointDetector(players, position)

    val group = RenderEntityGroup()

    val scale = 1.0f / bitmap.height
    val rotPerStride = -.5f / bitmap.width

    val currentOffset = Vector3f()
    val currentRotation = Quaternionf().rotateY(rotPerStride * -bitmap.width / 2)

    val strideRotation = Quaternionf().rotateY(rotPerStride)
    val stride = RIGHT_VECTOR.multiply(scale).toVector3f().rotate(currentRotation)

    // calculate width so we can offset it by half
    val widthStride = Vector3f(stride)
    for (x in 0 until bitmap.width) {
        widthStride.rotate(strideRotation)
        currentOffset.sub(widthStride)
    }
    currentOffset.mul(.5f)


    for (x in 0 until bitmap.width) {
        stride.rotate(strideRotation)
        currentOffset.add(stride)
        currentRotation.premul(strideRotation)

        val offset = Vector3f(currentOffset)
        val rotation = Quaternionf(currentRotation)

        for (y in 0 until bitmap.height) {
            val transform = Matrix4f()
                .rotate(quaternion)
                .translate(offset.x, offset.y + scale * y, offset.z)
                .rotate(rotation)
                .scale(scale)

            pointDetector
                .lookingAt(transform)
                .forEach { player -> onHover(player, x to y) }

            group.add(x to y, textRenderEntity(
                world = world,
                position = position,
                init = {
                    it.text = " "
                    it.brightness = Brightness(15, 15)
                },
                update = {
                    it.setTransformationMatrix(Matrix4f(transform).mul(textBackgroundTransform))
                    it.backgroundColor = bitmap[x to y]
                }
            ))

            if (cursor != null && cursor.first == x && cursor.second == y) {
                val cursorTransform = Matrix4f()
                    .rotate(quaternion)
                    .translate(offset.x, offset.y + scale * y, offset.z)
                    .rotate(rotation)
                    .scale(.03f)
                    .translate(.5f, .5f, .1f)
                    .rotateZ(Math.PI.toFloat() / 4)
                    .translate(-.5f, -.5f, .1f)
                    .mul(textBackgroundTransform)

                group.add("cursor", textRenderEntity(
                    world = world,
                    position = position,
                    init = {
                        it.text = " "
                        it.brightness = Brightness(15, 15)
                        it.interpolationDuration = 1
                    },
                    update = {
                        it.interpolateTransform(cursorTransform)
                        it.backgroundColor = Color.WHITE
                    }
                ))
            }
        }
    }

    return group
}
