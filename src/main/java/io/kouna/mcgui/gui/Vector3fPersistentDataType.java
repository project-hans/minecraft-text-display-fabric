package io.kouna.mcgui.gui;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class Vector3fPersistentDataType implements PersistentDataType<float[], Vector3f> {

    @Override
    public @NotNull Class<float[]> getPrimitiveType() {
        return float[].class;
    }

    @Override
    public @NotNull Class<Vector3f> getComplexType() {
        return Vector3f.class;
    }

    @Override
    public float @NotNull [] toPrimitive(@NotNull Vector3f vector3f, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        return new float[]{vector3f.x, vector3f.y, vector3f.z};
    }

    @Override
    public @NotNull Vector3f fromPrimitive(float @NotNull [] floats, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        if (floats.length != 3) {
            throw new IllegalStateException("Can not convert float array with length other than 3 to Vector3f.");
        }

        return new Vector3f(floats[0], floats[1], floats[2]);
    }
}
