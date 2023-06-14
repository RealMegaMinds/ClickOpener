package megaminds.clickopener.util;

import java.util.OptionalInt;

import megaminds.clickopener.Config;
import megaminds.clickopener.api.ClickType;
import megaminds.clickopener.api.HandlerRegistry;
import megaminds.clickopener.impl.Openable;
import megaminds.clickopener.impl.StackHolder;
import megaminds.clickopener.impl.UseAllower;
import megaminds.clickopener.screenhandlers.WrappedFactory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public class ScreenHelper {
	private ScreenHelper() {}

	public static OptionalInt openScreen(ServerPlayerEntity player, NamedScreenHandlerFactory fac) {
		//Save the cursor stack so it can be restored later and let player know we took the cursor stack
		var cursorStack = player.currentScreenHandler.getCursorStack();
		player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
		player.currentScreenHandler.syncState();	//also reverts picking up the item

		//Open the inventory (wrap so cursor doesn't reset)
		var syncId = player.openHandledScreen(WrappedFactory.wrap(fac));

		//Allow special inventories to work (ones that normally require a block in the world)
		if (player.currentScreenHandler instanceof UseAllower a) {
			a.clickOpener_allowUse();
		}

		//Restore the cursor stack and let player know
		player.currentScreenHandler.setCursorStack(cursorStack);
		player.currentScreenHandler.syncState();

		return syncId;
	}

	public static boolean openScreen(ServerPlayerEntity player, ClickType clickType, ItemStack stack, Inventory inventory) {
		var item = stack.getItem();
		if (!Config.isClickTypeAllowed(clickType) || !(item instanceof BlockItem bi) || !Config.isBlockItemAllowed(Registries.ITEM.getId(bi))) return false;

		var handler = HandlerRegistry.get(bi);
		if (handler == null || !handler.canCreateFactory(stack, player, inventory)) return false;

		var syncId = openScreen(player, handler.createFactory(stack, player, inventory));
		if (syncId.isPresent()) {
			final var h = player.currentScreenHandler;
			((Openable)(Object)stack).clickopener_open(()->{
				if (player.currentScreenHandler == h) {
					player.closeHandledScreen();
				}
			});
			((StackHolder)h).clickopener_setStack(stack);
		}
		return true;
	}
}
