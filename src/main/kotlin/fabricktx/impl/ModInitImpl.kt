package fabricktx.impl

import fabricktx.api.BlockList
import fabricktx.api.Registrar
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry



@PublishedApi internal class RegistryRegistrar<T>(private val registry: Registry<T>) : Registrar<T> {
    override fun register(toRegister: T, id: Identifier) {
        Registry.register(registry, id, toRegister)
    }
}

