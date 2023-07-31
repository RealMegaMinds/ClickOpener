package megaminds.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import megaminds.clickopener.api.HandlerRegisterEvent;
import megaminds.clickopener.api.HandlerRegistry;
import megaminds.clickopener.compat.ReinforcedShulkersCompat;
import megaminds.clickopener.compat.VanillaCompat;
import megaminds.clickopener.util.IdentifierAdapter;

public class ClickOpenerMod implements ModInitializer {
	public static final String MODID = "clickopener";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Identifier.class, new IdentifierAdapter())
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();
	public static final Config CONFIG = new Config();
	public static final PlayerConfigs PLAYER_CONFIGS = new PlayerConfigs();

	@Override
	public void onInitialize() {
		//TODO How long should keep old config reader?
		if (OldConfigConverter.shouldReadOldConfig()) {
			OldConfigConverter.fill(CONFIG);
		} else {
			CONFIG.reload();
		}
		PLAYER_CONFIGS.reload();

		CommandRegistrationCallback.EVENT.register(Commands::register);
		HandlerRegisterEvent.EVENT.register(HandlerRegisterEvent.VANILLA_PHASE, VanillaCompat::register);

		if (FabricLoader.getInstance().isModLoaded("reinfshulker")) {
			HandlerRegisterEvent.EVENT.register(ReinforcedShulkersCompat::register);
		}

		ServerLifecycleEvents.SERVER_STARTING.register(HandlerRegistry::onServerLoading);
	}
}
