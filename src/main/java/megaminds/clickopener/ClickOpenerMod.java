package megaminds.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import megaminds.clickopener.api.HandlerRegistry;
import megaminds.clickopener.util.Config;

public class ClickOpenerMod implements ModInitializer {
	public static final String MODID = "clickopener";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		Config.load();
		CommandRegistrationCallback.EVENT.register(Commands::register);
	}

	public static void open(BlockItem item, ServerPlayerEntity player) {
		var fac = HandlerRegistry.get(item).createFactory(null, player, null);
		player.openHandledScreen(fac);
	}
}
