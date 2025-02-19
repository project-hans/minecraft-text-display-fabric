package io.kouna.mcgui.gui

import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType

public interface PropertyBinding<T> {
    fun getValue(): T?
    fun setValue(value: T?): Unit
    fun fromEntity(entity: Entity): T?
    fun toEntity(entity: Entity): Unit
}

public class DataBinding<P: Any, C :Any> constructor(
    key: String,
    var value_: C? = null,
    private val type: PersistentDataType<P, C>
) : PropertyBinding<C> {
    private val namespacedKey = NamespacedKey.fromString(key)!!

    override fun getValue(): C? {
        return value_
    }

    override fun setValue(value: C?) {
        this.value_ = value
    }

    override fun fromEntity(entity: Entity): C? {
        val stored = entity.persistentDataContainer.get(namespacedKey, type)
        value_ = stored;
        return stored
    }

    override fun toEntity(entity: Entity) {
        if (value_ != null) {
            entity.persistentDataContainer.set(namespacedKey, type, value_!!)
        } else {
            entity.persistentDataContainer.remove(namespacedKey)
        }
    }
}

public class FunctionBinding<P: Any, C: Any> constructor(
    key: String,
    private val computeValue: (() -> C?),
    private val type: PersistentDataType<P, C>
) : PropertyBinding<C> {
    private val namespacedKey = NamespacedKey.fromString(key)!!
    private var value: C? = computeValue()

    override fun getValue(): C? {
        return value
    }

    override fun setValue(value: C?) {
        this.value = computeValue() ?: value
    }


    override fun fromEntity(entity: Entity): C? {
        val stored = entity.persistentDataContainer.get(namespacedKey, type)
        value = stored
        return stored
    }

    override fun toEntity(entity: Entity) {
        value = computeValue()
        if (value != null) {
            entity.persistentDataContainer.set(namespacedKey, type, value!!)
        } else {
            entity.persistentDataContainer.remove(namespacedKey)
        }
    }
}
