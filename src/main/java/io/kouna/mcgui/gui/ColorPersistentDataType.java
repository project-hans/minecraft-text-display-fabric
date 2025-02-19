package io.kouna.mcgui.gui;

import org.bukkit.Color;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ColorPersistentDataType implements PersistentDataType<Integer, Color> {
    @Override
    public @NotNull Class<Integer> getPrimitiveType() {
        return int.class;
    }

    @Override
    public @NotNull Class<Color> getComplexType() {
        return Color.class;
    }

    @Override
    public @NotNull Integer toPrimitive(@NotNull Color color, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        return color.asARGB();
    }

    @Override
    public @NotNull Color fromPrimitive(@NotNull Integer i, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        return Color.fromARGB(i);
    }
}
