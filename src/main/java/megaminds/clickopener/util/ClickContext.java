package megaminds.clickopener.util;

import megaminds.clickopener.api.ClickType;
import megaminds.clickopener.impl.Openable;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class ClickContext {
	private final ServerPlayerEntity player;
	private final Hand hand;
	private final Inventory clickedInventory;
	private final int slotIndex;
	private final ClickType clickType;
	private ItemStack stack;

	public ClickContext(ServerPlayerEntity player, Hand hand, Inventory clickedInventory, int slotIndex, ClickType clickType, ItemStack stack) {
		this.player = player;
		this.hand = hand;
		this.clickedInventory = clickedInventory;
		this.slotIndex = slotIndex;
		this.clickType = clickType;
		this.stack = stack;
	}

	public ClickContext(ClickContext context) {
		this(context.player, context.hand, context.clickedInventory, context.slotIndex, context.clickType, context.stack);
	}

	public ServerPlayerEntity player() {
		return player;
	}

	public Hand hand() {
		return hand;
	}

	public Inventory clickedInventory() {
		return clickedInventory;
	}

	public int slotIndex() {
		return slotIndex;
	}

	public ClickType clickType() {
		return clickType;
	}

	public BlockPos pos() {
		return player.getBlockPos();
	}

	public ItemStack stack() {
		return stack;
	}

	public void setStack(ItemStack stack) {
		clickedInventory.setStack(slotIndex, ItemStack.EMPTY);	//Set stack to empty while reassigning closer to try avoiding concurrency issues
		Openable.cast(stack).clickopener$setCloser(Openable.cast(this.stack).clickopener$clearCloser());
		this.stack = stack;
		clickedInventory.setStack(slotIndex, stack);
	}
}
