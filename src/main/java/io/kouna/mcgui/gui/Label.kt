package io.kouna.mcgui.gui

import org.bukkit.persistence.PersistentDataType

class Label constructor(text: String) : Element() {
    val text = bindings.add(DataBinding("gui:text", text, PersistentDataType.STRING))
}