package megaminds.clickopener.api;

import megaminds.clickopener.screenhandlers.AnvilItemScreenHandler;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface ItemScreenOpener {
	public static final ItemScreenOpener BLOCK_STATE_HANDLER = (ScreenFactoryOpener)(stack, player, i) -> ((BlockItem)stack.getItem()).getBlock().getDefaultState().createScreenHandlerFactory(player.getWorld(), player.getBlockPos());
	public static final ItemScreenOpener ANVIL_HANDLER = (ScreenFactoryOpener)(stack, p, inv) -> new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new AnvilItemScreenHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()), inv, stack), Text.translatable("container.repair"));
	public static final ItemScreenOpener SHULKER_BOX_HANDLER = ItemScreenOpener.requireSingleStack((ScreenFactoryOpener)(stack, p, i)->new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new ShulkerBoxScreenHandler(syncId, inventory, new ShulkerInventory(stack, 27, BlockEntityType.SHULKER_BOX)), stack.hasCustomName() ? stack.getName() : Text.translatable("container.shulkerBox")));

	boolean open(ItemStack stack, ServerPlayerEntity player, Inventory inventory);

	public static ItemScreenOpener requireSingleStack(ItemScreenOpener delegate) {
		return (stack, player, inventory) -> stack.getCount() == 1 && delegate.open(stack, player, inventory);
	}

	public interface ScreenFactoryOpener extends ItemScreenOpener {
		NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory);

		@Override
		default boolean open(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
			return player.openHandledScreen(createFactory(stack, player, inventory)).isPresent();
		}
	}
}
