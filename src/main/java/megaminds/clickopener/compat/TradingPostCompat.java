package megaminds.clickopener.compat;

import java.util.function.BiConsumer;

import fuzs.tradingpost.init.ModRegistry;
import megaminds.clickopener.ClickOpenerMod;
import megaminds.clickopener.api.ItemScreenOpener;
import net.minecraft.item.BlockItem;

public class TradingPostCompat {
	private TradingPostCompat() {}

	public static void register(BiConsumer<BlockItem, ItemScreenOpener> registryFunc) {
		registryFunc.accept((BlockItem) ModRegistry.TRADING_POST_ITEM.get(), ItemScreenOpener.BLOCK_USE_HANDLER);

		ClickOpenerMod.LOGGER.info("Trading Post Compat Loaded");
	}
}
