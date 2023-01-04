package megaminds.clickopener.api;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

public record ShulkerInventory(ItemStack link, int size, BlockEntityType<?> entityType, DefaultedList<ItemStack> inventory) implements Inventory {
	public static final String ITEMS_KEY = "Items";

	public ShulkerInventory(ItemStack link, int size, BlockEntityType<?> entityType) {
		this(link, size, entityType, DefaultedList.ofSize(size, ItemStack.EMPTY));
		var blockNbt = BlockItem.getBlockEntityNbt(link);
		if (blockNbt!=null) Inventories.readNbt(blockNbt, inventory);
	}

	private NbtCompound writeNbt() {
		return Inventories.writeNbt(new NbtCompound(), this.inventory, false);
	}

	@Override
	public void onClose(PlayerEntity player) {
		writeData();
	}

	@Override
	public void markDirty() {
		writeData();
	}

	private void writeData() {
		BlockItem.setBlockEntityNbt(link, entityType, writeNbt());
	}

	@Override
	public boolean isEmpty() {
		return inventory.stream().allMatch(i->i==null||i.isEmpty());
	}

	@Override
	public ItemStack getStack(int slot) {
		return inventory.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		var stack = Inventories.splitStack(inventory, slot, amount);
		if (!stack.isEmpty()) {
			this.markDirty();
		}
		return stack;
	}

	@Override
	public ItemStack removeStack(int slot) {
		var stack = Inventories.removeStack(inventory, slot);
		if (!stack.isEmpty()) {
			markDirty();
		}
		return stack;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (stack.getCount() > this.getMaxCountPerStack()) {
			stack.setCount(this.getMaxCountPerStack());
		}
		this.markDirty();
	}

	@Override
	public void clear() {
		inventory.clear();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity var1) {
		return true;
	}
}