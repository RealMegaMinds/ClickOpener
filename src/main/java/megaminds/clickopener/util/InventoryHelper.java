package megaminds.clickopener.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryHelper {
	private InventoryHelper() {}

	public static boolean canStackAddMore(ItemStack existingStack, ItemStack stack, int inventoryMaxCountPerStack) {
		return ItemStack.canCombine(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxCount() && existingStack.getCount() < inventoryMaxCountPerStack;
	}

	/**
	 * Only works for single items.
	 * Returned ItemStack is the one in the inventory (may be different) or null if can't fit.
	 */
	public static ItemStack addItemToInventory(Item item, Inventory inventory) {
		var stack = item.getDefaultStack();
		var slot = -1;
		for (int i = 0; i<inventory.size(); i++) {
			if (!inventory.isValid(i, stack)) continue;

			var existingStack = inventory.getStack(i);
			if (existingStack.isEmpty()) {
				if (slot==-1) slot = i;	//We want the first empty space only if there isn't anywhere else
			} else if (canStackAddMore(existingStack, stack, inventory.getMaxCountPerStack())) {
				existingStack.increment(1);
				slot = i;
				break;
			}
		}

		if (slot==-1) return null;

		return inventory.getStack(slot);
	}
}