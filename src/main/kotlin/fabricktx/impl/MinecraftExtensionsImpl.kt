package fabricktx.impl

import fabricktx.api.getBlock
import fabricktx.api.name
import fabricktx.api.xyz
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

@PublishedApi internal fun throwDiagnosticMessage(expectedBlockEntityName: String?,
                                                      actualBlockEntity: BlockEntity?,
                                                      pos: BlockPos,
                                                      world: IWorld
): Nothing {
    val readablePos = pos.xyz
    val blockEntityMessage = if (actualBlockEntity == null) "there is no block entity" else "it is a $actualBlockEntity"
    val block = pos.let { world.getBlock(it) }
    val side = world.name
    error("BlockEntity at location $readablePos is not a $expectedBlockEntityName as expected." +
            " Rather, $blockEntityMessage, and at the position exists a $block. This happened at the $side.")

}