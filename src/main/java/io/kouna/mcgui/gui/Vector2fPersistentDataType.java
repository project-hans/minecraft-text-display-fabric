package io.kouna.mcgui.gui;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

public class Vector2fPersistentDataType implements PersistentDataType<float[], Vector2f> {

    @Override
    public @NotNull Class<float[]> getPrimitiveType() {
        return float[].class;
    }

    @Override
    public @NotNull Class<Vector2f> getComplexType() {
        return Vector2f.class;
    }

    @Override
    public float @NotNull [] toPrimitive(@NotNull Vector2f vector2f, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        return new float[]{vector2f.x, vector2f.y};
    }

    @Override
    public @NotNull Vector2f fromPrimitive(float @NotNull [] floats, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        if (floats.length != 2) {
            throw new IllegalStateException("Can not convert float array with length other than 2 to Vector2f.");
        }

        return new Vector2f(floats[0], floats[1]);
    }
}
