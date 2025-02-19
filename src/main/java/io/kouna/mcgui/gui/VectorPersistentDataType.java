package io.kouna.mcgui.gui;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class VectorPersistentDataType implements PersistentDataType<double[], Vector> {
    @Override
    public @NotNull Class<double[]> getPrimitiveType() {
        return double[].class;
    }

    @Override
    public @NotNull Class<Vector> getComplexType() {
        return Vector.class;
    }

    @Override
    public double @NotNull [] toPrimitive(@NotNull Vector vector, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        return new double[]{vector.getX(), vector.getY(), vector.getZ()};
    }

    @Override
    public @NotNull Vector fromPrimitive(double @NotNull [] doubles, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        return new Vector(doubles[0], doubles[1], doubles[2]);
    }
}
