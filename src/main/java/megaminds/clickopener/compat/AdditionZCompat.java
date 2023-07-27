package megaminds.clickopener.compat;

import java.util.function.BiConsumer;

import megaminds.clickopener.ClickOpenerMod;
import megaminds.clickopener.api.ItemScreenOpener;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;

public class AdditionZCompat {
	private AdditionZCompat() {}

	public static void register(BiConsumer<BlockItem, ItemScreenOpener> registryFunc) {
		registryFunc.accept((BlockItem)Items.FLETCHING_TABLE, ItemScreenOpener.BLOCK_STATE_HANDLER);

		ClickOpenerMod.LOGGER.info("AdditionZ Compat Loaded");
	}
}
