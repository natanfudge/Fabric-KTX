@file:Suppress("NOTHING_TO_INLINE")

package fabricktx.api

import com.mojang.datafixers.util.Pair
import fabricktx.impl.RegistryRegistrar
import fabricktx.impl.blockEntityTypeRegistry
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.gui.screen.ingame.ContainerScreen
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.container.BlockContext
import net.minecraft.container.Container
import net.minecraft.container.PlayerContainer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Function

/**
 * Should be called at the init method of the mod. Do all of your registry here.
 */
inline fun initCommon(modId: String, group: ItemGroup? = null, init: CommonModInitializationContext.() -> Unit) {
    CommonModInitializationContext(modId, group).init()
}


/**
 * Should be called at the client init method
 */
inline fun initClientOnly(modId: String, init: ClientModInitializationContext.() -> Unit) {
    ClientModInitializationContext(modId).init()
}


class CommonModInitializationContext(
    @PublishedApi internal val modId: String,
    @PublishedApi internal val group: ItemGroup?
) {

    inline fun <T> registerTo(registry: Registry<T>, init: IdentifierRegistryContext<T>.() -> Unit) =
        IdentifierRegistryContext(modId, RegistryRegistrar(registry)).init()

    inline fun registerBlocks(init: BlockListRegistryContext.() -> Unit) = BlockListRegistryContext(modId, group).init()

    fun registerContainer(containerId: Identifier, factory: (Int, PlayerInventory, BlockContext) -> Container) {
        ContainerProviderRegistry.INSTANCE.registerFactory(containerId) { syncId, _, player, buf ->
            factory(
                syncId,
                player.inventory,
                BlockContext.create(player.world, buf.readBlockPos())
            )
        }
    }

}

class ClientModInitializationContext(@PublishedApi internal val modId: String) {

    fun Block.setRenderLayer(renderLayer: RenderLayer) = BlockRenderLayerMap.INSTANCE.putBlock(this, renderLayer)

    fun <T : BlockEntity> registerBlockEntityRenderer(
        be: BlockEntityType<T>,
        rendererFactory: (BlockEntityRenderDispatcher?) -> BlockEntityRenderer<T>
    ) {
        BlockEntityRendererRegistry.INSTANCE.register(be, rendererFactory)
    }

    fun registerKeyBinding(keyBinding: KotlinKeyBinding): Boolean = KeyBindingRegistry.INSTANCE.register(keyBinding)

    fun registerKeyBindingCategory(name: String) = KeyBindingRegistry.INSTANCE.addCategory(name)

    fun registerBlockModel(blockPath: String, vararg textures: Identifier, bakery: () -> BakedModel) {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider {
            ModelVariantProvider { modelId, _ ->
                if (modelId.namespace == modId && modelId.path == blockPath) {
                    object : UnbakedModel {
                        override fun getModelDependencies(): List<Identifier> = listOf()


                        override fun bake(
                            loader: ModelLoader?, textureGetter: Function<SpriteIdentifier, Sprite>?,
                            rotationContainer: ModelBakeSettings?, modelId: Identifier?
                        ): BakedModel? =
                            bakery()

                        override fun getTextureDependencies(
                            unbakedModelGetter: Function<Identifier, UnbakedModel>?,
                            unresolvedTextureReferences: MutableSet<Pair<String, String>>?
                        ):
                                List<SpriteIdentifier> =
                            textures.map { SpriteIdentifier(PlayerContainer.BLOCK_ATLAS_TEXTURE, it) }

                    }
                } else null
            }
        }
    }

    fun <C : Container> registerScreen(
        screenId: Identifier,
        controllerFactory: (Int, PlayerInventory, BlockContext) -> C,
        screenFactory: (C, PlayerEntity) -> ContainerScreen<C>
    ) {
        ScreenProviderRegistry.INSTANCE.registerFactory(screenId) { syncId, _, player, buf ->
            screenFactory(
                controllerFactory(
                    syncId,
                    player.inventory,
                    BlockContext.create(player.world, buf.readBlockPos())
                ),
                player
            )
        }
    }
}


class IdentifierRegistryContext<T>(private val namespace: String, private val registrar: Registrar<T>) {
    infix fun T.withId(id: Identifier) = registrar.register(this, id)
    infix fun T.withId(name: String) = withId(Identifier(namespace, name))
}

class BlockListRegistryContext(@PublishedApi internal val namespace: String, @PublishedApi internal val group: ItemGroup?) {
    fun <T : Block> T.withId(name: String, registerItem: Boolean = true) {
        registerBlockWithItem(this, Identifier(namespace, name), registerItem)
    }

    fun <BE : BlockEntity, T : StateBlock<BE>> T.withId(name: String, registerItem: Boolean = true) {
        (this as Block).withId(name, registerItem)
        Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            Identifier(namespace, name),
            this.blockEntityType(listOf(this)).also { blockEntityTypeRegistry[this] = it })
    }

    // Overload needed because you can't have optional parameters with infix functions
    inline infix fun <BE : BlockEntity, T : StateBlock<BE>> T.withId(name: String) = withId(name, registerItem = true)

    inline fun <T : Block> BlockList<T>.withId(registerItem: Boolean = true, nameProvider: (T) -> String) {
        for (block in this) {
            registerBlockWithItem(block, Identifier(namespace, nameProvider(block)), registerItem)
        }
        if (blockEntityType != null) Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            Identifier(namespace, nameProvider(first())),
            blockEntityType
        )
    }

    @PublishedApi
    internal fun registerBlockWithItem(block: Block, id: Identifier, registerItem: Boolean) {
        Registry.register(Registry.BLOCK, id, block)
        if (registerItem) Registry.register(
            Registry.ITEM, id, BlockItem(
                block, Item.Settings().group(
                    group
                        ?: ItemGroup.MISC
                )
            )
        )
    }
}


interface Registrar<T> {
    fun register(toRegister: T, id: Identifier)
}





