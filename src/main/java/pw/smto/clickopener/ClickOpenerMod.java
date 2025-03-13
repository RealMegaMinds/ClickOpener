package pw.smto.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pw.smto.clickopener.api.OpenerRegistry;
import pw.smto.clickopener.util.IdentifierAdapter;

//When using item on block, check item is allowed
//Add option for ticking screenhandler when itemstack ticks
//Create feedback translations
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
		CONFIG.reload();
		PLAYER_CONFIGS.reload();

		CommandRegistrationCallback.EVENT.register(Commands::register);
		ServerLifecycleEvents.SERVER_STARTING.register(OpenerRegistry::onServerLoading);
	}
}
