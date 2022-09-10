package megaminds.clickopener.util;

import megaminds.clickopener.api.ClickType;
import megaminds.clickopener.api.HandlerRegistry;
import megaminds.clickopener.impl.CloseIgnorer;
import megaminds.clickopener.impl.UseAllower;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

public class ScreenHelper {
	public static void openScreen(ServerPlayerEntity player, NamedScreenHandlerFactory fac) {		
		var cursorStack = player.currentScreenHandler.getCursorStack();
		player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
		syncCursor(player);	//Stops ghost item

		if (player.currentScreenHandler!=player.playerScreenHandler) {
			((CloseIgnorer)player).clickOpener_ignoreNextClose();	//ensures mouse doesn't reset
		} else {
			player.closeScreenHandler();
		}

		player.openHandledScreen(fac);

		if (player.currentScreenHandler instanceof UseAllower a) {
			a.clickOpener_allowUse();
		}

		player.currentScreenHandler.setCursorStack(cursorStack);	//set cursor stack in current screen to what it was in the old one
		syncCursor(player);
	}

	public static void syncCursor(ServerPlayerEntity player) {
		player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, player.currentScreenHandler.nextRevision(), -1, player.currentScreenHandler.getCursorStack().copy()));
	}

	public static boolean openScreen(ServerPlayerEntity player, ClickType clickType, ItemStack stack, Inventory inventory) {
		var item = stack.getItem();
		if (!Config.isClickTypeAllowed(clickType) || !(item instanceof BlockItem bi) || !Config.isBlockItemAllowed(Registry.ITEM.getId(bi))) return false;

		var handler = HandlerRegistry.get(bi);
		if (handler == null) return false;

		openScreen(player, handler.createFactory(stack, player, inventory));
		return true;
	}
}
