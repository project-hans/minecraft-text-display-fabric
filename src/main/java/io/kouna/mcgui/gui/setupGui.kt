package io.kouna.mcgui.gui

import io.kouna.mcgui.utilities.EntityTag
import io.kouna.mcgui.utilities.onTick
import io.kouna.mcgui.utilities.rendering.SharedEntityRenderer
import org.bukkit.Color
import org.bukkit.util.Vector

fun setupGui() {
    val myGui = Panel().let { elem ->
        elem.backgroundColor = Color.FUCHSIA
        elem.size.setX(10f)
        elem.size.setY(.5f)
        elem.addChild(Panel()) {
            it.backgroundColor = Color.LIME
            it.size.setX(4f)
            it.size.setY(1f)
            it.position.setZ(0.1f)
        }

        elem
    }

    onTick {
        for (entity in EntityTag("gui:sample").getEntities().take(1)) {
            if (myGui.entityHandle?.entity != null) {
                SharedEntityRenderer.render(myGui.entityHandle?.entity to ::setupGui, myGui.prepareRenderGroup())
            } else {
                SharedEntityRenderer.render(entity to ::setupGui, myGui.toAnchor(entity))
            }

        }
    }
}