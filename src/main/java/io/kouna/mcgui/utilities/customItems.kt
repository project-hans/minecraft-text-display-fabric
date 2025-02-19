package io.kouna.mcgui.utilities

import org.bukkit.Bukkit
import org.bukkit.Bukkit.createInventory
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

val customItemRegistry = mutableListOf<ItemStack>()

fun openCustomItemInventory(player: Player) {
    val inventory = createInventory(null, 9 * 3, "Items")
    customItemRegistry.forEach { inventory.addItem(it) }
    player.openInventory(inventory)
}

class CustomItemComponent(val id: String) {
    fun isItem(item: ItemStack): Boolean {
        val key = NamespacedKey(currentPlugin, "item_component_$id")
        return item.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.BOOLEAN) == true
    }

    fun attach(item: ItemStack) {
        val itemMeta = item.itemMeta ?: return
        val key = NamespacedKey(currentPlugin, "item_component_$id")
        itemMeta.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, true)
        item.itemMeta = itemMeta
    }

    fun onGestureUse(action: (Player, ItemStack) -> Unit) {
        onGestureUseItem { player, item ->
            if (isItem(item)) action(player, item)
        }
    }

    fun onInteractEntity(action: (Player, Entity, ItemStack) -> Unit) {
        io.kouna.mcgui.utilities.onInteractEntity(fun (player, entity, hand) {
            val item = player.inventory.getItem(hand) ?: return
            if (isItem(item)) action(player, entity, item)
        })
    }

    fun onHeldTick(action: (Player, ItemStack) -> Unit) {
        onTick {
            for (player in Bukkit.getServer().onlinePlayers) {
                val itemInMainHand = player.inventory.itemInMainHand
                val itemInOffHand = player.inventory.itemInOffHand
                if (isItem(itemInMainHand)) action(player, itemInMainHand)
                if (isItem(itemInOffHand)) action(player, itemInOffHand)
            }
        }
    }
}

fun createNamedItem(material: org.bukkit.Material, name: String): ItemStack {
    val item = ItemStack(material)
    val itemMeta = item.itemMeta ?: throw Exception("ItemMeta is null")
    itemMeta.setItemName(ChatColor.RESET.toString() + name)
    item.itemMeta = itemMeta
    return item
}

fun ItemStack.attach(component: CustomItemComponent): ItemStack {
    component.attach(this)
    return this
}