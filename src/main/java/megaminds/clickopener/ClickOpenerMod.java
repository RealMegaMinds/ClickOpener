package megaminds.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import megaminds.clickopener.api.OpenerRegistry;
import megaminds.clickopener.util.IdentifierAdapter;

//FIXME AM I INCLUDING SHULKER TOOLTIPS ON ACCIDENT?
//TODO AM I INCLUDING SHULKER TOOLTIPS ON ACCIDENT?

//implement Item.useOnBlock option for when holding items and clicking
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

	//Add option for ticking screenhandler when itemstack ticks
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

//		if (FabricLoader.getInstance().isModLoaded("supplementaries")) {
//			HandlerRegisterEvent.EVENT.register(SupplementariesCompat::register);
//		}

		ServerLifecycleEvents.SERVER_STARTING.register(OpenerRegistry::onServerLoading);
	}
}
