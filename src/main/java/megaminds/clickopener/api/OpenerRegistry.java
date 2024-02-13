package megaminds.clickopener.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.BlockItem;
import net.minecraft.server.MinecraftServer;

public class OpenerRegistry {
	private static final Map<BlockItem, ItemScreenOpener> ITEM_SCREEN_OPENERS = new HashMap<>();

	private OpenerRegistry() {}

	/**
	 * If in singleplayer, this only loads for the first server.
	 */
	@SuppressWarnings("unused")
	public static void onServerLoading(MinecraftServer server) {
		if (ITEM_SCREEN_OPENERS.isEmpty()) {
			OpenerRegisterEvent.EVENT.invoker().onRegister(OpenerRegistry::register);
		}
	}

	private static void register(BlockItem item, ItemScreenOpener opener) {
		ITEM_SCREEN_OPENERS.put(item, opener);
	}

	public static ItemScreenOpener getOrDefault(BlockItem item) {
		return ITEM_SCREEN_OPENERS.getOrDefault(item, ItemScreenOpener.DEFAULT_OPENER);
	}
}
