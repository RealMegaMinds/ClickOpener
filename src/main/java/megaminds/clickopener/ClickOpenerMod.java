package megaminds.clickopener;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClickOpenerMod implements ModInitializer {
	public static final String MODID = "clickopener";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
		dispatcher.register(CommandManager.literal(MODID)
				.then(CommandManager.literal("reload")
						.executes(context->{
							Config.load();
							context.getSource().sendFeedback(Text.of("ClickOpener Config Reloaded"), false);
							return 1;
						}))));

		Config.load();
	}
}
