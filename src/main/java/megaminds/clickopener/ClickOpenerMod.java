package megaminds.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClickOpenerMod implements ModInitializer {
	public static final String MODID = "clickopener";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		Config.load();
		CommandRegistrationCallback.EVENT.register(Commands::register);
	}
}
