package io.kouna.mcgui.gui

import io.kouna.mcgui.textBackgroundTransform
import io.kouna.mcgui.utilities.EntityTag
import io.kouna.mcgui.utilities.getQuaternion
import io.kouna.mcgui.utilities.getString
import io.kouna.mcgui.utilities.rendering.*
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginLogger
import org.bukkit.util.Vector
import org.joml.Matrix4f
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class HierarchyHandle constructor(
    val parent: Element, val child: Element, val root: Element) {}
class EntityHandle<T> constructor(val element: T, val entity: Entity) {}

val DEFAULT_BACKGROUND = Color.fromARGB(0x7f000000);
open class Element constructor(val id: UUID = UUID.randomUUID()) {
    companion object Utils {
        val logger = PluginLogger.getLogger("GUI")
        val controlTypeKey = NamespacedKey.fromString("gui:control_type")!!
        val controlIdKey = NamespacedKey.fromString("gui:control_id")!!
    }

    class Bindings {
        private val bindings = HashSet<PropertyBinding<Any>>()

        fun <T> add(binding: PropertyBinding<T>): PropertyBinding<T> {
            bindings.add(binding as PropertyBinding<Any>)
            return binding
        }

        fun apply(entity: Entity) {
            for (binding in bindings) {
                binding.toEntity(entity)
            }
        }

        fun load(entity: Entity) {
            for (binding in bindings) {
                binding.fromEntity(entity)
            }
        }
    }

    class AnchorParams constructor(val entity: Entity, element: Element) {
        val anchorPosition = entity.location.toVector()
        val quaternion = entity.location.getQuaternion()
        val offset = element.position.toVector3f()
        val transform = Matrix4f().rotate(quaternion).translate(offset)
        val group = element.prepareRenderGroup()
    }

    class RenderHandle<T: Entity> constructor(
        private val createForAnchor: (handle: RenderHandle<T>, anchor: AnchorParams) -> RenderEntity<T>,
        private var renderEntity: RenderEntity<T>? = null
    ) {
        val onRemoveHandlers: HashSet<((RenderHandle<*>) -> Unit)> = HashSet()

        fun create(params: AnchorParams): RenderEntity<T> {
            return createForAnchor(this, params)
        }

        fun remove() {
            for (handler in onRemoveHandlers) {
                handler(this)
            }
        }
    }

    class Renders : Iterable<RenderHandle<*>> {
        private val handles: HashSet<RenderHandle<*>> = HashSet()

        fun <T: Entity> add(
            createForAnchor: (handle: RenderHandle<T>, params: AnchorParams) -> RenderEntity<T>,
        ): RenderHandle<T> {
            val handle = RenderHandle(createForAnchor)
            handle.onRemoveHandlers.add { handles.remove(it) }
            handles.add(handle)

            return handle
        }

        override fun iterator(): Iterator<RenderHandle<*>> {
            return handles.iterator()
        }
    }

    val children = HashMap<UUID, HierarchyHandle>()
    protected val bindings = Bindings()
    protected val renders = Renders()
    val groupTag = EntityTag("gui:controlGroup:$id")
    var parentHandle: HierarchyHandle? = null
    var entityHandle: EntityHandle<*>? = null
    val position = Vector(0f, 0f, 1e-6f)
    var size = Vector(1f, 1f, 0f)
    var backgroundColor = DEFAULT_BACKGROUND

    fun querySelector(query: String): Element {
        throw NotImplementedError()
    }

    fun querySelectorAll(query: String): Collection<Element> {
        throw NotImplementedError()
    }

    fun <T: Element> instanceFromEntity(entity: Entity, classType: Class<T>? = null): T {
        val idString = entity.persistentDataContainer.getString(controlIdKey)
            ?: throw Exception("entity:${entity.entityId} did not have a UUID assigned to $controlIdKey, deleting.");

        val uuid = UUID.fromString(idString)
        if (children.containsKey(uuid)) {
            val instance = childById<Element>(uuid)
            if (instance == null) {
                children.remove(uuid)
            } else {
                return instance as T
            }
        }

        val instance = classType ?: Class.forName(entity.persistentDataContainer.getString(controlTypeKey) ?: Element::class.qualifiedName)
            .getConstructor(UUID::class.java)
            .newInstance(uuid)
        return instance as T
    }

    fun <T: Element> childById(uuid: UUID): T? {
        return children[uuid]?.child as T?;
    }

    fun append(parentElement: Element) {
        parentElement.addChild(this)
    }

    fun getRoot(): Element {
        return this.parentHandle?.root ?: this
    }

    fun <T: Element> addChild(element: T, with: ((T) -> Unit)? = null): Element {
        if (children.containsKey(element.id)) {
            throw Exception("control:${element.id} is already member of group:${id}")
        }

        val handle = HierarchyHandle(child = element, parent = this, root = getRoot());
        children[element.id] = handle;

        // remove child from its previous parent
        element.parentHandle?.parent?.removeChild(element);

        // set the new parent handle to the child
        element.parentHandle = handle;
        with?.invoke(element)
        return this
    }

    fun <T: Element> removeChild(element: T, with: ((T) -> Unit)? = null): Element {
        this.children.remove(element.id)

        element.parentHandle = null
        // remove parents group tag from the child
        element.entityHandle?.entity?.let { groupTag.remove(it) }
        with?.invoke(element)

        return this
    }

    private var renderGroup_: RenderEntityGroup? = null;
    fun prepareRenderGroup(): RenderEntityGroup {
        if (getRoot().renderGroup_ == null) {
            val group = RenderEntityGroup();
            getRoot().renderGroup_ =  group;
            return group
        }
        return getRoot().renderGroup_!!
    }

    val baseRender = renders.add { handle, params ->
        textRenderEntity(
            location = params.anchorPosition.toLocation(params.entity.world),
            init = {
                it.text(Component.text(" "))
                it.displayWidth = this.size.x.toFloat()
                it.displayHeight = this.size.y.toFloat()
                it.interpolationDuration = 1

                for(child in children) {
                    child.value.child.toAnchor(it)
                }
            },
            update = {
                it.backgroundColor = backgroundColor
                it.interpolateTransform(Matrix4f(params.transform).mul(textBackgroundTransform).scale(size.toVector3f()))
            }
        )
    }

    fun toAnchor(entity: Entity): RenderEntityGroup {
        val params = AnchorParams(entity, this);

        for (handle in renders) {
            val renderEntity = handle.create(params)
            val key = entity to handle;
            params.group.add(key, renderEntity)
            handle.onRemoveHandlers.add {
                params.group.items.remove(key)
            }
        }

        return params.group
    }
}