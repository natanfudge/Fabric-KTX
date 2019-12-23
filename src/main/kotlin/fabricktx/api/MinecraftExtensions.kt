@file:Suppress("NOTHING_TO_INLINE")

package fabricktx.api

import fabricktx.impl.throwDiagnosticMessage
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.LongTag
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.IWorld
import net.minecraft.world.World
import kotlin.math.sqrt


val BlockPos.xz get() = "($x,$z)"
val BlockPos.xyz get() = "(x = $x,y = $y,z = $z)"

fun BlockPos.distanceFrom(otherPos: Vec3d) =
        sqrt((otherPos.x - this.x).squared() + (otherPos.y - this.y).squared() + (otherPos.z - this.z).squared())


operator fun BlockPos.plus(other: BlockPos): BlockPos = this.add(other)
operator fun BlockPos.plus(vec3d: Vec3d): Vec3d = this.toVec3d() + vec3d
operator fun BlockPos.minus(other: BlockPos): BlockPos = this.subtract(other)
operator fun BlockPos.minus(other: Vec3d): Vec3d = this.toVec3d().subtract(other)

fun BlockPos.toVec3d() = Vec3d(this)


fun CompoundTag.putBlockPos(key: String, pos: BlockPos?) = if (pos != null) putLong(key, pos.asLong()) else Unit

fun CompoundTag.getBlockPos(key: String): BlockPos? {
    val tag = get(key) ?: return null
    if (tag !is LongTag) return null
    return BlockPos.fromLong(tag.long)
}

fun vec3d(x: Double, y: Double, z: Double) = Vec3d(x, y, z)
operator fun Vec3d.plus(other: Vec3d) = Vec3d(this.x + other.x, this.y + other.y, this.z + other.z)
operator fun Vec3d.minus(other: Vec3d): Vec3d = this.subtract(other)


fun IWorld.play(soundEvent: SoundEvent, at: BlockPos,
                ofCategory: SoundCategory, toPlayer: PlayerEntity? = null, volumeMultiplier: Float = 1.0f, pitchMultiplier: Float = 1.0f) {
    playSound(toPlayer, at, soundEvent, ofCategory, volumeMultiplier, pitchMultiplier)
}

fun IWorld.getBlock(location: BlockPos): Block = getBlockState(location).block

fun IWorld.setBlock(block: Block, pos: BlockPos, blockState: BlockState = block.defaultState): Boolean = world.setBlockState(pos, blockState)

val World.isServer get() = !isClient

val IWorld.name get() = if (isClient) "Client" else "Server"

fun IWorld.dropItemStack(stack: ItemStack, pos: BlockPos): ItemEntity = dropItemStack(stack, pos.toVec3d())


fun IWorld.dropItemStack(stack: ItemStack, pos: Vec3d): ItemEntity =
        ItemEntity(world, pos.x, pos.y, pos.z, stack).also {
            world.spawnEntity(it)
        }




inline fun Ingredient.matches(itemStack: ItemStack) = test(itemStack)

/**
 * Note that what is held in the main hand still exists in the inventory, so it includes that.
 */
val PlayerEntity.itemsInInventoryAndOffhand get() = inventory.main + inventory.offHand


fun PlayerEntity.isHoldingItemIn(hand: Hand): Boolean = !getStackInHand(hand).isEmpty

fun PlayerEntity.offerOrDrop(itemStack: ItemStack) = inventory.offerOrDrop(world, itemStack)

fun PlayerEntity.openGui(id: Identifier, pos: BlockPos) = ContainerProviderRegistry.INSTANCE.openContainer(id, this)
{ it.writeBlockPos(pos) }

operator fun VoxelShape.plus(other : VoxelShape) : VoxelShape = VoxelShapes.union(this,other)

fun VoxelShape.union(vararg others :  VoxelShape) : VoxelShape = VoxelShapes.union(this,*others)

fun BlockPos.adjacentPositions() = listOf(up(), down(), south(), west(), north(), east())