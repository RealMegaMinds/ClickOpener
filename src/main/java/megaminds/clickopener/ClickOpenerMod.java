package megaminds.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import megaminds.clickopener.util.Config;

//TODO Add duplication checking (if person takes shulkerbox out of chest while someone else is looking into it)
//TODO Stop ItemStack from being able to be moved when it is opened
public class ClickOpenerMod implements ModInitializer {
	public static final String MODID = "clickopener";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		Config.load();
		CommandRegistrationCallback.EVENT.register(Commands::register);
	}
}
