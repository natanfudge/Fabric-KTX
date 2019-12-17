package fabricktx.impl

import fabricktx.api.copy
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction
import kotlin.math.min

private fun areItemsEqual(stack1: ItemStack, stack2: ItemStack): Boolean {
    return stack1.item === stack2.item && ItemStack.areTagsEqual(stack1, stack2)
}

private fun Inventory.stackIsNotEmptyAndCanAddMore(toStack: ItemStack, stackToAdd: ItemStack): Boolean {
    return !toStack.isEmpty &&
            areItemsEqual(toStack, stackToAdd)
            && toStack.isStackable
            && toStack.count < toStack.maxCount
            && toStack.count < this.invMaxStackAmount
}

private fun Inventory.availableSlots(direction: Direction): Iterable<Int> {
    return if (this is SidedInventory) getInvAvailableSlots(direction).toList() else (0 until invSize)
}

private fun Inventory.canInsert(slot: Int, stack: ItemStack, direction: Direction): Boolean {
    return if (this is SidedInventory) canInsertInvStack(slot, stack, direction) else isValidInvStack(slot, stack)
}

internal fun Inventory.distributeToAvailableSlots(stack: ItemStack, acceptEmptySlots: Boolean, direction: Direction): ItemStack {
    val maxStackSize = min(invMaxStackAmount, stack.maxCount)
    var stackCountLeftToDistribute = stack.count
    for (slot in availableSlots(direction)) {
        if (!canInsert(slot, stack, direction)) continue

        val stackInSlot = getInvStack(slot)
        if ((acceptEmptySlots && stackInSlot.isEmpty) || stackIsNotEmptyAndCanAddMore(stackInSlot, stack)) {
            val amountThatCanFitInSlot = maxStackSize - stackInSlot.count
            if (amountThatCanFitInSlot >= 0) {
                setInvStack(slot, ItemStack(stack.item,
                    min(maxStackSize, stackInSlot.count + stackCountLeftToDistribute)
                )
                )
                stackCountLeftToDistribute -= amountThatCanFitInSlot
            }
        }

        if (stackCountLeftToDistribute <= 0) return ItemStack.EMPTY

    }

    return stack.copy(count = stackCountLeftToDistribute)
}