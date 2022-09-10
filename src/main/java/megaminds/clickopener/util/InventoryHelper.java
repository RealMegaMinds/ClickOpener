package megaminds.clickopener.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class InventoryHelper {
	private InventoryHelper() {}
	
	public static boolean canStackAddMore(ItemStack existingStack, ItemStack stack, int inventoryMaxCountPerStack) {
		return !existingStack.isEmpty() && ItemStack.canCombine(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxCount() && existingStack.getCount() < inventoryMaxCountPerStack;
	}

	/**
	 * Returns remainder
	 */
	public static ItemStack addStackToInventory(ItemStack stack, Inventory inventory) {
		for (int i = 0; i<inventory.size(); i++) {	//NOSONAR
			if (!inventory.isValid(i, stack)) continue;

			var existingStack = inventory.getStack(i);
			if (existingStack.isEmpty()) {
				inventory.setStack(i, stack);
			} else if (canStackAddMore(existingStack, stack, inventory.getMaxCountPerStack())) {
				var diff = Math.min(inventory.getMaxCountPerStack(), existingStack.getMaxCount()) - existingStack.getCount();
				diff = Math.min(diff, stack.getCount());
				existingStack.increment(diff);
				stack.decrement(diff);
				if (stack.isEmpty()) break;
			}
		}
		return stack;
	}
}