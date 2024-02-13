package megaminds.clickopener.util;

import megaminds.clickopener.ClickOpenerMod;
import megaminds.clickopener.api.ItemScreenOpener;
import megaminds.clickopener.api.OpenerRegistry;
import megaminds.clickopener.impl.ClosePacketSkipper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class ScreenHelper {
	private ScreenHelper() {}

	public static boolean openScreen(ClickContext clickContext, ItemScreenOpener opener) {
		var context = opener.constructContext(clickContext);

		var previous = context.player().currentScreenHandler;

		//Save the cursor stack so it can be restored later and let player know we took the cursor stack
		var cursorStack = previous.getCursorStack();
		previous.setCursorStack(ItemStack.EMPTY);
		previous.syncState();	//also reverts picking up the item

		//Open the inventory (skip the close packet to prevent the cursor position from resetting)
		((ClosePacketSkipper)context.player()).clickopener$setSkipClosePacket(true);
		opener.preOpen(context);
		opener.open(context);
		var success = context.player().currentScreenHandler != previous;
		((ClosePacketSkipper)context.player()).clickopener$setSkipClosePacket(false);

		if (success) {
			opener.postOpen(context);
		}

		//Restore the cursor stack and let player know
		context.player().currentScreenHandler.setCursorStack(cursorStack);
		context.player().currentScreenHandler.syncState();

		return success;
	}
	
	public static boolean openScreen(ClickContext clickContext) {
		//TODO Remove when adding non-block functionality
		if (!(clickContext.stack().getItem() instanceof BlockItem bi)) return false;

		if (clickContext.stack().getCount() != 1 || !ClickOpenerMod.PLAYER_CONFIGS.isClickTypeAllowed(clickContext.player(), clickContext.clickType()) || !ClickOpenerMod.CONFIG.isAllowed(bi)) {
			return false;
		}

		try {
			return openScreen(clickContext, OpenerRegistry.getOrDefault(bi));
		} catch (ItemOpenException e) {
			ClickOpenerMod.LOGGER.warn("Error opening item", e);
		}
		return false;
	}
}
