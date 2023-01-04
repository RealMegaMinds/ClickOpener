package megaminds.clickopener.api;

import java.util.HashMap;
import java.util.Map;

import megaminds.clickopener.compat.ReinforcedShulkersCompat;
import megaminds.clickopener.compat.VanillaCompat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.BlockItem;

public class HandlerRegistry {
	private static final Map<BlockItem, ItemScreenHandler> ITEM_SCREEN_HANDLERS = new HashMap<>();
	private static boolean handlersLoaded = false;

	private HandlerRegistry() {}

	public static void register(BlockItem item, ItemScreenHandler handler) {
		ITEM_SCREEN_HANDLERS.put(item, handler);
	}

	public static ItemScreenHandler get(BlockItem item) {
		if (!handlersLoaded) loadHandlers();
		return ITEM_SCREEN_HANDLERS.get(item);
	}


	private static void loadHandlers() {
		VanillaCompat.init();

		if (FabricLoader.getInstance().isModLoaded("reinfshulker")) {
			ReinforcedShulkersCompat.init();
		}

		handlersLoaded = true;
	}
}
