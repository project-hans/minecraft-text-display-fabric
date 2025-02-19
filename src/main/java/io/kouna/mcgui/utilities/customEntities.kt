package io.kouna.mcgui.utilities

import org.bukkit.Bukkit
import org.bukkit.entity.Entity

class EntityTag(
    private val tag: String
)  {
    fun set(entity: Entity) {
        entity.scoreboardTags.add(tag)
    }

    fun remove(entity: Entity) {
        entity.scoreboardTags.remove(tag)
    }

    fun getEntities() = allEntities().filter { it.scoreboardTags.contains(tag) }

    fun onTick(action: (Entity) -> Unit) {
        io.kouna.mcgui.utilities.onTick {
            getEntities().forEach { action(it) }
        }
    }

    fun onInteract(action: (event: org.bukkit.event.player.PlayerInteractEntityEvent) -> Unit) {
        onInteractEntity { event ->
            if (!event.rightClicked.scoreboardTags.contains(tag)) return@onInteractEntity
            action(event)
        }
    }
}

fun allEntities() = Bukkit.getServer().worlds.flatMap { it.entities }