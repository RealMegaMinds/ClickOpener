package megaminds.clickopener.api;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ItemScreenOpener {
	@SuppressWarnings("deprecation")
	public static final ItemScreenOpener BLOCK_USE_HANDLER = (stack, player, i) -> {
		var block = ((BlockItem)stack.getItem()).getBlock();
		block.onUse(block.getDefaultState(), player.getServerWorld(), player.getBlockPos(), player, null, null);
	};
	public static final ItemScreenOpener BLOCK_STATE_HANDLER = (ScreenFactoryOpener)(stack, player, i) -> ((BlockItem)stack.getItem()).getBlock().getDefaultState().createScreenHandlerFactory(player.getWorld(), player.getBlockPos());

	void open(ItemStack stack, ServerPlayerEntity player, Inventory inventory);

	@SuppressWarnings("unused")
	default void afterSuccess(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {}

	public static ItemScreenOpener requireSingleStack(ItemScreenOpener delegate) {
		return (stack, player, inventory) -> {
			if (stack.getCount() == 1) {
				delegate.open(stack, player, inventory);
			}
		};
	}

	public interface ScreenFactoryOpener extends ItemScreenOpener {
		NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory);

		@Override
		default void open(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
			player.openHandledScreen(createFactory(stack, player, inventory));
		}
	}
}
