package fabricktx.impl

import fabricktx.api.Registrar
import fabricktx.api.StateBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry


@PublishedApi
internal class RegistryRegistrar<T>(private val registry: Registry<T>) : Registrar<T> {
    override fun register(toRegister: T, id: Identifier) {
        Registry.register(registry, id, toRegister)
    }
}

/**
 * We want to use the BlockEntityType of a BlockEntity in the registry and then use the same instance in the BlockEntity constructor
 * , so we must save the instance
 */
internal val blockEntityTypeRegistry = BlockEntityRegistry(mutableMapOf())

internal inline class BlockEntityRegistry(private val pairs: MutableMap<StateBlock<*>, BlockEntityType<*>>) {
    operator fun <T : BlockEntity> get(key: StateBlock<T>): BlockEntityType<T>? = pairs[key] as BlockEntityType<T>?
    operator fun <T : BlockEntity> set(key: StateBlock<T>, value: BlockEntityType<T>) = pairs.put(key, value)
}