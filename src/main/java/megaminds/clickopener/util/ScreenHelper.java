package megaminds.clickopener.util;

import java.util.function.Function;

import megaminds.clickopener.ClickOpenerMod;
import megaminds.clickopener.api.ClickType;
import megaminds.clickopener.api.HandlerRegistry;
import megaminds.clickopener.api.ItemScreenOpener;
import megaminds.clickopener.impl.ClosePacketSkipper;
import megaminds.clickopener.impl.Openable;
import megaminds.clickopener.impl.StackHolder;
import megaminds.clickopener.impl.UseAllower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ScreenHelper {
	private ScreenHelper() {}

	public static boolean openScreen(ServerPlayerEntity player, ItemStack stack, Inventory inventory, ItemScreenOpener opener) {
		//Save the cursor stack so it can be restored later and let player know we took the cursor stack
		var cursorStack = player.currentScreenHandler.getCursorStack();
		player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
		player.currentScreenHandler.syncState();	//also reverts picking up the item

		//Open the inventory (skip the close packet to prevent the cursor position from resetting)
		((ClosePacketSkipper)player).clickopener$setSkipClosePacket(true);
		var success = opener.open(stack, player, inventory);
		((ClosePacketSkipper)player).clickopener$setSkipClosePacket(false);

		//Allow special inventories to work (ones that normally require a block in the world)
		if (success && player.currentScreenHandler instanceof UseAllower a) {
			a.clickOpener_allowUse();
		}

		//Restore the cursor stack and let player know
		player.currentScreenHandler.setCursorStack(cursorStack);
		player.currentScreenHandler.syncState();

		return success;
	}

	public static boolean openScreen(ServerPlayerEntity player, ClickType clickType, ItemStack stack, Inventory inventory) {
		var item = stack.getItem();
		if (!ClickOpenerMod.PLAYER_CONFIGS.isClickTypeAllowed(player, clickType) || !(item instanceof BlockItem bi) || !ClickOpenerMod.CONFIG.isAllowed(bi)) return false;

		var handler = HandlerRegistry.get(bi);
		if (handler == null) return false;//TODO Use a best guess default if config enabled

		if (openScreen(player, stack, inventory, handler)) {
			final var h = player.currentScreenHandler;
			((Openable)(Object)stack).clickopener$setCloser(()->{
				if (player.currentScreenHandler == h) {
					player.closeHandledScreen();
				}
			});
			((StackHolder)h).clickopener$setOpenStack(stack);
		}
		return true;
	}

	/**
	 * Requires inventory size to be multiple of 9 with max size of 9x6.
	 */
	public static ScreenHandlerFactory genericScreenHandlerFactoryFor(Function<PlayerEntity, Inventory> inventoryProducer) {
		return (syncId, playerInventory, player) -> {
			var inventory = inventoryProducer.apply(player);
			var rowCount = inventory.size() / 9;
			var type = Registries.SCREEN_HANDLER.get(new Identifier("generic_9x"+rowCount));
			return new GenericContainerScreenHandler(type, syncId, playerInventory, inventory, rowCount);
		};
	}
}
