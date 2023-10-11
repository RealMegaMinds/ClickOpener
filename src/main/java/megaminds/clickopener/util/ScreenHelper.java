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
import megaminds.clickopener.screenhandlers.DelegatedInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class ScreenHelper {
	private ScreenHelper() {}

	public static boolean openScreen(ServerPlayerEntity player, ItemStack stack, Inventory inventory, ItemScreenOpener opener) {
		var previous = player.currentScreenHandler;

		//Save the cursor stack so it can be restored later and let player know we took the cursor stack
		var cursorStack = previous.getCursorStack();
		previous.setCursorStack(ItemStack.EMPTY);
		previous.syncState();	//also reverts picking up the item

		//Open the inventory (skip the close packet to prevent the cursor position from resetting)
		((ClosePacketSkipper)player).clickopener$setSkipClosePacket(true);
		opener.open(stack, player, inventory);
		var success = player.currentScreenHandler != previous;
		((ClosePacketSkipper)player).clickopener$setSkipClosePacket(false);

		//Allow special inventories to work (ones that normally require a block in the world)
		if (success) {
			((UseAllower) player.currentScreenHandler).clickopener$allowUse();
			opener.afterSuccess(stack, player, inventory);
		}

		//Restore the cursor stack and let player know
		player.currentScreenHandler.setCursorStack(cursorStack);
		player.currentScreenHandler.syncState();

		return success;
	}

	public static boolean openScreen(ServerPlayerEntity player, ClickType clickType, ItemStack stack, Inventory inventory) {
		var item = stack.getItem();
		if (!ClickOpenerMod.PLAYER_CONFIGS.isClickTypeAllowed(player, clickType) || !(item instanceof BlockItem bi) || !ClickOpenerMod.CONFIG.isAllowed(bi)) {
			return false;
		}

		var handler = HandlerRegistry.get(bi);
		if (handler == null) {
			handler = ItemScreenOpener.BLOCK_USE_HANDLER;
			ClickOpenerMod.LOGGER.debug("Using default handler.");
		}

		try {
			if (openScreen(player, stack, inventory, handler)) {
				final var h = player.currentScreenHandler;
				((Openable)(Object)stack).clickopener$setCloser(()->{
					if (player.currentScreenHandler == h) {
						player.closeHandledScreen();
					}
				});
				((StackHolder)h).clickopener$setOpenStack(stack);
				return true;
			}
		} catch (RuntimeException e) {
			ClickOpenerMod.LOGGER.warn("Error opening item", e);
		}

		ClickOpenerMod.LOGGER.atDebug().setMessage("Failed to open screen for {}.").addArgument(() -> Registries.ITEM.getId(item)).log();
		return false;
	}

	/**
	 * Requires max size of 54
	 */
	public static ScreenHandlerFactory adjustableSizeFactoryFor(Function<PlayerEntity, Inventory> inventoryProducer, boolean shouldValidateSlots) {
		return (syncId, playerInventory, player) -> {
			var inventory = inventoryProducer.apply(player);
			if (inventory.size() > 54) {
				throw new IllegalArgumentException("Cannot create inventory of size: "+inventory.size());
			}

			var rowCount = getRowCountFor(inventory.size());
			var displaySize = rowCount * 9;
			var invToUse = !shouldValidateSlots && displaySize == inventory.size() ? inventory : new DelegatedInventory(inventory, displaySize, shouldValidateSlots);
			return new GenericContainerScreenHandler(getGenericTypeForRowCount(rowCount), syncId, playerInventory, invToUse, rowCount);
		};
	}

	public static int getRowCountFor(int size) {
		return (int) Math.ceil(size / 9.0);
	}

	public static ScreenHandlerType<GenericContainerScreenHandler> getGenericTypeForRowCount(int rowCount) {
		return switch (rowCount) {
			case 1 -> ScreenHandlerType.GENERIC_9X1;
			case 2 -> ScreenHandlerType.GENERIC_9X2;
			case 3 -> ScreenHandlerType.GENERIC_9X3;
			case 4 -> ScreenHandlerType.GENERIC_9X4;
			case 5 -> ScreenHandlerType.GENERIC_9X5;
			case 6 -> ScreenHandlerType.GENERIC_9X6;
			default -> throw new IllegalArgumentException("rowCount must be >0 and <=6");
		};
	}
}
