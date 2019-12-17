package fabricktx.api

import fabricktx.impl.throwDiagnosticMessage
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import java.util.function.Supplier


/**
 * A state block is a block with a block entity.
 *
 * In this file 4 classes:
 * Block superclasses: [MultipleStateBlock], [SingularStateBlock]
 * Block entity superclass: [KBlockEntity]
 * Block container: [BlockList]
 *
 * Whenever you make a block that has MULTIPLE instances, you MUST:
 * - Use [MultipleStateBlock] as the block superclass.
 * - Store blocks in a [BlockList] and register it.
 *
 * Whenever you make a block that has ONE instance (a singleton), you SHOULD:
 * - Use [SingularStateBlock] as the block superclass.
 * - Declare the block as an `object` and access and register it that way.
 *
 * In both cases [KBlockEntity] needs to be the [BlockEntity] superclass.
 *
 * Registration is done thorough [CommonModInitializationContext.registerBlocks].
 */

//internal typealias StateBlock<T>  = MultipleStateBlock<T>

abstract class MultipleStateBlock<T : BlockEntity>(
        settings: Settings,
        /**
         * The SAME [blockEntityProducer] must be given for multiple blocks of the same class.
         */
        internal val blockEntityProducer: () -> T
) : BlockEntityProvider, Block(settings) {
    override fun createBlockEntity(view: BlockView) = blockEntityProducer()
}

abstract class SingularStateBlock<T : BlockEntity>(
        settings: Settings,
        /**
         * The SAME [blockEntityProducer] must be given for multiple blocks of the same class.
         */
        blockEntityProducer: () -> T
) : MultipleStateBlock<T>(settings, blockEntityProducer) {
    val blockEntityType = BlockEntityType(Supplier { blockEntityProducer() }, setOf(this), null)
    override fun createBlockEntity(view: BlockView) = blockEntityProducer()
}


class BlockList<T : Block>(private val blocks: List<T>) : List<T> by blocks {
    constructor(vararg blocks: T) : this(blocks.toList())

    init {
        require(blocks.isNotEmpty())
    }

    val blockEntityType: BlockEntityType<*>? = run {
        val first = blocks.first()
        if (first is MultipleStateBlock<*>) {
            BlockEntityType(Supplier { first.blockEntityProducer() }, blocks.toSet(), null)
        } else null
    }
}

abstract class KBlockEntity private constructor(type: BlockEntityType<*>) : BlockEntity(type) {
    constructor(block: SingularStateBlock<*>) : this(block.blockEntityType)
    constructor(blocks: BlockList<*>) : this(blocks.blockEntityType
            ?: error("Impossible because BlockWithBlockEntity defines a block entity type"))
}

inline fun <reified T : BlockEntity> BlockEntity?.assertIs(pos: BlockPos, world: IWorld): T {
    return this as? T ?: throwDiagnosticMessage(T::class.qualifiedName, this, pos, world)
}





