package fabricktx.api

import fabricktx.impl.blockEntityTypeRegistry
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
 * In this file 3 classes:
 * Block superclass: [StateBlock]
 * Block entity superclass: [KBlockEntity]
 * Block container: [BlockList]
 *
 * Whenever you make a block that has MULTIPLE instances, you MUST:
 * - Store blocks in a [BlockList] and register it.
 *
 * Whenever you make a block that has ONE instance (a singleton), you SHOULD:
 * - Declare the block as an `object` and access and register it that way.
 *
 * In both cases [StateBlock] needs to be the [Block] superclass, and [KBlockEntity] needs to be the [BlockEntity] superclass.
 *
 * Registration is done thorough [CommonModInitializationContext.registerBlocks].
 */

abstract class StateBlock<T : BlockEntity>(
        settings: Settings,
        /**
         * The SAME [blockEntityProducer] must be given for multiple blocks of the same class.
         */
        internal val blockEntityProducer: () -> T
) : BlockEntityProvider, Block(settings) {
    override fun createBlockEntity(view: BlockView) = blockEntityProducer()
}

val<T : BlockEntity> StateBlock<T>.blockEntityType
    get()  : BlockEntityType<T> = blockEntityTypeRegistry[this]
            ?: error("$this must be registered as an individual block to access its blockEntityType. " +
                    "If its registered as a BlockList then it must get the BlockEntityType from the BlockList.")


class BlockList<T : Block>(private val blocks: List<T>) : List<T> by blocks {
    constructor(vararg blocks: T) : this(blocks.toList())

    init {
        require(blocks.isNotEmpty())
    }

    val blockEntityType: BlockEntityType<*>? = run {
        val first = blocks.first()
        if (first is StateBlock<*>) first.blockEntityType(blocks)
        else null
    }
}

@PublishedApi
internal fun <T : Block,BE : BlockEntity> StateBlock<BE>.blockEntityType(blocks: List<T>): BlockEntityType<BE> =
        BlockEntityType(Supplier { blockEntityProducer() }, blocks.toSet(), null)

abstract class KBlockEntity private constructor(type: BlockEntityType<*>) : BlockEntity(type) {
    constructor(block: StateBlock<*>) : this(blockEntityTypeRegistry[block]
            ?: error("$block used the BlockList constructor for the block entity and registered it as a singular block," +
                    " it must register the BlockList and not the individual blocks!")
    )

    constructor(blocks: BlockList<*>) : this(
            blocks.blockEntityType
                    ?: error("The BlockList must contain StateBlocks that correspond to this BlockEntity!")
    )
}

inline fun <reified T : BlockEntity> BlockEntity?.assertIs(pos: BlockPos, world: IWorld): T {
    return this as? T ?: throwDiagnosticMessage(T::class.qualifiedName, this, pos, world)
}


