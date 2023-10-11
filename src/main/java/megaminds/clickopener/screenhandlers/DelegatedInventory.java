package megaminds.clickopener.screenhandlers;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

/**
 * All inventory operations are passed through except where {@link #isFake(int)} == true.
 */
public class DelegatedInventory implements Inventory {
	private static final ItemStack EMPTY = Items.WHITE_STAINED_GLASS_PANE.getDefaultStack().setCustomName(Text.empty());

	private final Inventory delegate;
	private final int displaySize;
	private final boolean shouldValidateSlots;

	public DelegatedInventory(Inventory delegate, int displaySize, boolean shouldValidateSlots) {
		this.delegate = delegate;
		this.displaySize = displaySize;
		this.shouldValidateSlots = shouldValidateSlots;

		if (displaySize < delegate.size()) {
			throw new IllegalArgumentException("displaySize < delegate.size()");
		}
	}

	public DelegatedInventory(Inventory delegate, int displaySize) {
		this(delegate, displaySize, false);
	}

	public boolean isFake(int index) {
		return index >= delegateSize() && index < displaySize;
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int size() {
		return displaySize;
	}

	public int delegateSize() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public ItemStack getStack(int var1) {
		if (isFake(var1)) {
			return EMPTY;
		}
		return delegate.getStack(var1);
	}

	@Override
	public ItemStack removeStack(int var1, int var2) {
		if (isFake(var1)) {
			return EMPTY;
		}
		return delegate.removeStack(var1, var2);
	}

	@Override
	public ItemStack removeStack(int var1) {
		if (isFake(var1)) {
			return EMPTY;
		}
		return delegate.removeStack(var1);
	}

	@Override
	public void setStack(int var1, ItemStack var2) {
		if (isFake(var1)) return;

		delegate.setStack(var1, var2);
	}

	@Override
	public void markDirty() {
		delegate.markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity var1) {
		return delegate.canPlayerUse(var1);
	}

	@Override
	public int getMaxCountPerStack() {
		return delegate.getMaxCountPerStack();
	}

	@Override
	public void onOpen(PlayerEntity player) {
		delegate.onOpen(player);
	}

	@Override
	public void onClose(PlayerEntity player) {
		delegate.onClose(player);
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if (isFake(slot)) {
			return false;
		}
		return delegate.isValid(slot, stack);
	}

	@Override
	public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
		if (isFake(slot)) {
			return false;
		}
		return delegate.canTransferTo(hopperInventory, slot, stack);
	}

	@Override
	public int count(Item item) {
		return delegate.count(item);
	}

	@Override
	public boolean containsAny(Set<Item> items) {
		return delegate.containsAny(items);
	}

	@Override
	public boolean containsAny(Predicate<ItemStack> predicate) {
		return delegate.containsAny(predicate);
	}

	public boolean shouldValidateSlots() {
		return shouldValidateSlots;
	}
}
