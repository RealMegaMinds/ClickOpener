package megaminds.clickopener.api;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface ItemScreenOpener {
	@SuppressWarnings("deprecation")
	public static final ItemScreenOpener BLOCK_USE_HANDLER = (stack, player, i) -> {
		var block = ((BlockItem)stack.getItem()).getBlock();
		return block.onUse(block.getDefaultState(), player.getServerWorld(), player.getBlockPos(), player, null, null) == ActionResult.CONSUME;
	};
	public static final ItemScreenOpener BLOCK_STATE_HANDLER = (ScreenFactoryOpener)(stack, player, i) -> ((BlockItem)stack.getItem()).getBlock().getDefaultState().createScreenHandlerFactory(player.getWorld(), player.getBlockPos());

	boolean open(ItemStack stack, ServerPlayerEntity player, Inventory inventory);

	public static ItemScreenOpener requireSingleStack(ItemScreenOpener delegate) {
		return (stack, player, inventory) -> stack.getCount() == 1 && delegate.open(stack, player, inventory);
	}

	public interface ScreenFactoryOpener extends ItemScreenOpener {
		NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory);

		@SuppressWarnings("unused")
		default void afterSuccess(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {}

		@Override
		default boolean open(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
			var success = player.openHandledScreen(createFactory(stack, player, inventory)).isPresent();
			if (success) {
				afterSuccess(stack, player, inventory);
			}
			return success;
		}
	}
}
