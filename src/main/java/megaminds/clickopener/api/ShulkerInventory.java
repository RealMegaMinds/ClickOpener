package megaminds.clickopener.api;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class ShulkerInventory implements Inventory, NamedScreenHandlerFactory {
	public static final int INVENTORY_SIZE = 27;
	public static final String ITEMS_KEY = "Items";
	public static final String NAME_KEY = "CustomName";

	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
	private Text customName;
	private ItemStack link;

	public ShulkerInventory(ItemStack link) {
		this.link = link;
		readNbt();
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
		BlockItem.setBlockEntityNbt(link, BlockEntityType.SHULKER_BOX, writeNbt());
	}

	private void readNbt() {
		if (link.hasCustomName()) {
			this.customName = link.getName();
		}

		var blockNbt = BlockItem.getBlockEntityNbt(link);
		this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		if (blockNbt!=null && blockNbt.contains(ITEMS_KEY, NbtElement.LIST_TYPE)) {
			Inventories.readNbt(blockNbt, this.inventory);
		}
	}

	private NbtCompound writeNbt() {
		return Inventories.writeNbt(new NbtCompound(), this.inventory, false);
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new ShulkerBoxScreenHandler(syncId, playerInventory, this);
	}

	@Override
	public Text getDisplayName() {
		if (customName != null) {
			return customName;
		}
		return Text.translatable("container.shulkerBox");
	}

	@Override
	public boolean isEmpty() {
		return inventory.stream().allMatch(ItemStack::isEmpty);
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
	public int size() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean canPlayerUse(PlayerEntity var1) {
		return true;
	}
}