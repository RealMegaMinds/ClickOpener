package megaminds.clickopener.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.BlockItem;
import net.minecraft.server.MinecraftServer;

public class HandlerRegistry {
	private static final Map<BlockItem, ItemScreenOpener> ITEM_SCREEN_HANDLERS = new HashMap<>();

	private HandlerRegistry() {}

	/**
	 * If in singleplayer, this only loads for the first server.
	 */
	@SuppressWarnings("unused")
	public static void onServerLoading(MinecraftServer server) {
		if (ITEM_SCREEN_HANDLERS.isEmpty()) {
			HandlerRegisterEvent.EVENT.invoker().onRegister(HandlerRegistry::register);
		}
	}

	private static void register(BlockItem item, ItemScreenOpener handler) {
		ITEM_SCREEN_HANDLERS.put(item, handler);
	}

	public static ItemScreenOpener get(BlockItem item) {
		return ITEM_SCREEN_HANDLERS.get(item);
	}
}
