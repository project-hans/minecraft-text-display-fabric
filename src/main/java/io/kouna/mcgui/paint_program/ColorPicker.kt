package io.kouna.mcgui.paint_program

import io.kouna.mcgui.utilities.hsv
import io.kouna.mcgui.utilities.toHSV
import org.bukkit.Color

object ColorPicker {
    private var selectedPrivate: Color = Color.fromRGB(255, 0, 0)

    var selected: Color
        get() = selectedPrivate
        set(color) {
            selectedPrivate = color

            val (h,s,v) = color.toHSV()
            HuePicker.hue = h.toInt()
            SVPicker.sv = s to v
        }


    init {
        fun updateColor() {
            selectedPrivate = hsv(HuePicker.hue.toDouble(), SVPicker.sv.first, SVPicker.sv.second)
        }

        HuePicker.onPick = ::updateColor
        SVPicker.onPick = ::updateColor
    }
}