package fabricktx.api

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

//TODO: continue attempt
interface IBlock {
    fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, placedStack: ItemStack) {}
    fun onRemoved(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {}
    val settings: Block.Settings get() = Block.Settings.of(Material.METAL)
}

val IBlock.block get() = BlockInterfaceWrapper(this)

class BlockInterfaceWrapper(private val block: IBlock) : Block(block.settings) {
    override fun onPlaced(
        world: World, blockPos: BlockPos, blockState: BlockState, livingEntity: LivingEntity?,
        itemStack: ItemStack
    ) {
        super.onPlaced(world, blockPos, blockState, livingEntity, itemStack)
        block.onPlaced(world, blockPos, blockState, livingEntity, itemStack)
    }

    override fun onBlockRemoved(blockState: BlockState, world: World, blockPos: BlockPos, blockState2: BlockState, bl: Boolean) {
        block.onRemoved(blockState, world, blockPos, blockState2, bl)
        super.onBlockRemoved(blockState, world, blockPos, blockState2, bl)
    }
}