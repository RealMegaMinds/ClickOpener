package megaminds.clickopener;

import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

public class ShulkerInventory implements SidedInventory, NamedScreenHandlerFactory {
	private static final int[] AVAILABLE_SLOTS = IntStream.range(0, 27).toArray();

	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
	private final ItemStack link;

	public ShulkerInventory(ItemStack link) {
		this.link = link;
		var nbt = BlockItem.getBlockEntityNbt(link);
		if (nbt!=null) Inventories.readNbt(nbt, inventory);
	}

	@Override
	public void markDirty() {
		var nbt = new NbtCompound();
		Inventories.writeNbt(nbt, inventory, false);
		BlockItem.setBlockEntityNbt(link, BlockEntityType.SHULKER_BOX, nbt);
	}

	@Override
	public int size() {
		return this.inventory.size();
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return AVAILABLE_SLOTS;
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return this.inventory.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack itemStack = Inventories.splitStack(this.inventory, slot, amount);
		if (!itemStack.isEmpty()) {
			this.markDirty();
		}
		return itemStack;
	}

	@Override
	public ItemStack removeStack(int slot) {
		return Inventories.removeStack(this.inventory, slot);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);
		if (stack.getCount() > this.getMaxCountPerStack()) {
			stack.setCount(this.getMaxCountPerStack());
		}
		this.markDirty();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void clear() {
		this.inventory.clear();
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity var3) {
		return new ShulkerBoxScreenHandler(syncId, playerInventory, this);
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText("container.shulkerBox");
	}
}