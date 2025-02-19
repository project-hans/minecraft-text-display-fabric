package io.kouna.mcgui.gui

class StackLayout(val orientation: StackLayout.Orientation, val children: List<Element> = ArrayList()) {
    enum class Orientation {
        HORIZONTAL,
        VERTICAL
    }
}
