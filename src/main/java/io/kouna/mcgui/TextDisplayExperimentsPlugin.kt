package io.kouna.mcgui

import io.kouna.mcgui.bitmaps.setupBitmapDisplay
import io.kouna.mcgui.gui.setupGui
import io.kouna.mcgui.paint_program.setupPaintProgram
import io.kouna.mcgui.particle_system.setupParticleSystem
import io.kouna.mcgui.point_detector_visualizer.setUpPointDetectorVisualizer
import io.kouna.mcgui.utilities.*
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.joml.*

val textBackgroundTransform: Matrix4f; get() = Matrix4f()
    .translate(-0.1f + .5f,-0.5f + .5f,0f)
    .scale(8.0f,4.0f,1f) //  + 0.003f  + 0.001f

@Suppress("unused")
class TextDisplayExperimentsPlugin : JavaPlugin() {
    override fun onDisable() {
        logger.info("Disabling Text Display Experiments plugin")
        closeCurrentPlugin()
    }

    override fun onEnable() {
        logger.info("Enabling Text Display Experiments plugin")

        currentPlugin = this

        setupBitmapDisplay()
        setupTextEntityUtilities()
        setupPaintProgram()
        setupCloak()
        setupParticleSystem()
        setupColoredCauldrons()
        setUpPointDetectorVisualizer()
        setupGui()

        getCommand("items")?.setExecutor { sender, _, _, _ ->
            openCustomItemInventory(sender as? Player ?: run {
                sender.sendMessage("Only players can use this command")
                return@setExecutor true
            })
            true
        }

//        getCommand("player_set_data")?.apply {
//            setExecutor { sender, _, _, args ->
//                val entitySelector = args.getOrNull(1) ?: run {
//                    sender.sendMessage("No entity selector provided")
//                    return@setExecutor true
//                }
//
//                val player = entitySelector.let {
//                    Bukkit.selectEntities(sender, it).firstOrNull() as? Player
//                } ?: run {
//                    sender.sendMessage("No player found")
//                    return@setExecutor true
//                }
//
//                val key = args.getOrNull(2)?.let { NamespacedKey.fromString(it) } ?: run {
//                    sender.sendMessage("No key provided")
//                    return@setExecutor true
//                }
//
//                val value = args.getOrNull(3) ?: run {
//                    sender.sendMessage("No value provided")
//                    return@setExecutor true
//                }
//
//                player.persistentDataContainer.
//
//
//                sender.sendMessage("Modified player data")
//                true
//            }
//        }
    }
}
