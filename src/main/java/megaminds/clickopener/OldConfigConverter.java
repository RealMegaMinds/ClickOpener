package megaminds.clickopener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class OldConfigConverter {
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(ClickOpenerMod.MODID+".properties");
	private static final String CLICK_TYPE_KEY = "clickType";
	private static final String DEFAULT_KEY = "default";

	private OldConfigConverter() {}

	public static boolean shouldReadOldConfig() {
		return Files.exists(CONFIG_FILE) && !Files.exists(Config.CONFIG_FILE);
	}

	public static void fill(Config config) {
		//Read in properties
		var properties = new Properties();
		try (var in = Files.newInputStream(CONFIG_FILE)) {
			properties.load(in);
		} catch (IOException e) {
			ClickOpenerMod.LOGGER.error("Failed to read configuration file");
		}

		//Fill config
		config.reset();
		var def = Boolean.parseBoolean((String)properties.computeIfAbsent(DEFAULT_KEY, k->"false"));
		if (def) {
			ClickOpenerMod.LOGGER.error("Cannot convert old config: New config requires a whitelist.");
			return;
		}

		properties.forEach((key, value)->{
			if (key instanceof String k && value instanceof String v && !CLICK_TYPE_KEY.equals(key) && !DEFAULT_KEY.equals(key) && Boolean.parseBoolean(v)) {
				config.addBlockItem(new Identifier(k), true, false);
			}
		});

		//Delete old config
		try {
			Files.delete(CONFIG_FILE);
		} catch (IOException e) {
			ClickOpenerMod.LOGGER.error("Failed to delete old configuration file. You may want to delete it yourself.");
		}

		//Write new config
		config.write();
	}
}
