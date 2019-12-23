package fabricktx.api

import fabricktx.impl.distributeToAvailableSlots
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

fun World.inventoryExistsIn(pos: BlockPos): Boolean = world.getBlock(pos) is InventoryProvider
        || world.getBlockEntity(pos) is Inventory


fun World.getInventoryIn(pos: BlockPos): Inventory? {
    val blockEntityInventory = world.getBlockEntity(pos)

    // Fuck you notch
    if (blockEntityInventory is ChestBlockEntity) {
        val blockState = world.getBlockState(pos)
        val block = blockState.block
        if (block is ChestBlock) {
            return ChestBlock.getInventory(block, blockState, this, pos, true)
        }
    }

    if (blockEntityInventory is Inventory) return blockEntityInventory
    val blockState = world.getBlockState(pos)
    return (blockState.block as? InventoryProvider)?.getInventory(blockState, this, pos)
}


fun Inventory.getAllItems(): List<ItemStack> = List(invSize) { getInvStack(it) }

fun itemStackList(size: Int): DefaultedList<ItemStack> = DefaultedList.ofSize(size, ItemStack.EMPTY)
fun ItemStack.equalsIgnoreCount(other: ItemStack) = ItemStack.areItemsEqual(this, other) && ItemStack.areTagsEqual(this, other)
fun ItemStack.copy(count: Int): ItemStack = copy().apply { this.count = count }

/**
 * Returns the remaining stack
 */
fun Inventory.insert(stack: ItemStack, direction: Direction = Direction.UP): ItemStack {
    val remainingAfterNonEmptySlots = distributeToAvailableSlots(stack, acceptEmptySlots = false, direction = direction)
    return distributeToAvailableSlots(remainingAfterNonEmptySlots, acceptEmptySlots = true, direction = direction)
}

object EmptyInventory : SidedInventory {
    override fun getInvAvailableSlots(var1: Direction?): IntArray = IntArray(0)
    override fun canExtractInvStack(var1: Int, var2: ItemStack?, var3: Direction?): Boolean = false
    override fun canInsertInvStack(var1: Int, var2: ItemStack?, var3: Direction?): Boolean = false
    override fun clear() {}
    override fun getInvSize(): Int = 0
    override fun isInvEmpty(): Boolean = true
    override fun getInvStack(var1: Int): ItemStack = ItemStack.EMPTY
    override fun takeInvStack(var1: Int, var2: Int): ItemStack = ItemStack.EMPTY
    override fun removeInvStack(var1: Int): ItemStack = ItemStack.EMPTY
    override fun setInvStack(var1: Int, var2: ItemStack) {}
    override fun markDirty() {}
    override fun canPlayerUseInv(var1: PlayerEntity): Boolean = false
}

interface ImplementedInventory : SidedInventory {
    /**
     * Gets the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    val items: DefaultedList<ItemStack>
    /**
     * Returns the inventory size.
     */
    override fun getInvSize(): Int {
        return items.size
    }

    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    override fun isInvEmpty(): Boolean {
        for (i in 0 until invSize) {
            val stack = getInvStack(i)
            if (!stack.isEmpty) {
                return false
            }
        }
        return true
    }

    /**
     * Gets the item in the slot.
     */
    override fun getInvStack(slot: Int): ItemStack {
        return items[slot]
    }

    /**
     * Takes a stack of the size from the slot.
     *
     * (default implementation) If there are less items in the slot than what are requested,
     * takes all items in that slot.
     */
    override fun takeInvStack(slot: Int, count: Int): ItemStack {
        val result = Inventories.splitStack(items, slot, count)
        if (!result.isEmpty) {
            markDirty()
        }
        return result
    }

    /**
     * Removes the current stack in the `slot` and returns it.
     */
    override fun removeInvStack(slot: Int): ItemStack {
        return Inventories.removeStack(items, slot)
    }

    /**
     * Replaces the current stack in the `slot` with the provided stack.
     *
     * If the stack is too big for this inventory ([Inventory.getInvMaxStackAmount]),
     * it gets resized to this inventory's maximum amount.
     */
    override fun setInvStack(slot: Int, stack: ItemStack) {
        items[slot] = stack
        if (stack.count > invMaxStackAmount) {
            stack.count = invMaxStackAmount
        }
    }

    /**
     * Clears [the item list][.getItems]}.
     */
    override fun clear() {
        items.clear()
    }

    override fun markDirty() {
        // Override if you want behavior.
    }

    override fun canPlayerUseInv(player: PlayerEntity): Boolean {
        return true
    }

    override fun getInvAvailableSlots(direction: Direction): IntArray {
        // Just return an array of all slots
        val result = IntArray(items.size)
        for (i in result.indices) {
            result[i] = i
        }

        return result
    }

    override fun canInsertInvStack(slot: Int, stack: ItemStack, direction: Direction?): Boolean {
        return true
    }

    override  fun canExtractInvStack(slot: Int, stack: ItemStack, direction: Direction): Boolean {
        return true
    }

    companion object {
        // Creation
        /**
         * Creates an inventory from the item list.
         */
        fun of(items: DefaultedList<ItemStack>): ImplementedInventory = object :
            ImplementedInventory {
            override val items = items
        }

        /**
         * Creates a new inventory with the size.
         */
        fun ofSize(size: Int): ImplementedInventory {
            return of(
                DefaultedList.ofSize(
                    size,
                    ItemStack.EMPTY
                )
            )
        }
    }
}