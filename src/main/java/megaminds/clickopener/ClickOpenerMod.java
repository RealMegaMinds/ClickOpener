package megaminds.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import megaminds.clickopener.api.HandlerRegisterEvent;
import megaminds.clickopener.api.HandlerRegistry;
import megaminds.clickopener.compat.ReinforcedShulkersCompat;
import megaminds.clickopener.compat.VanillaCompat;

public class ClickOpenerMod implements ModInitializer {
	public static final String MODID = "clickopener";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		Config.load();
		CommandRegistrationCallback.EVENT.register(Commands::register);
		HandlerRegisterEvent.EVENT.register(HandlerRegisterEvent.VANILLA_PHASE, VanillaCompat::register);

		if (FabricLoader.getInstance().isModLoaded("reinfshulker")) {
			HandlerRegisterEvent.EVENT.register(ReinforcedShulkersCompat::register);
		}

		ServerLifecycleEvents.SERVER_STARTING.register(HandlerRegistry::onServerLoading);
	}
}
