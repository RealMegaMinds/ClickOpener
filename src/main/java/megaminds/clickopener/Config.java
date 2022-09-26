package megaminds.clickopener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import megaminds.clickopener.api.ClickType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class Config {
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(ClickOpenerMod.MODID+".properties");
	private static final String CLICK_TYPE_KEY = "clickType";
	private static final String DEFAULT_KEY = "default";

	private static final List<Identifier> NON_DEFAULT_BLOCK_ITEMS = new ArrayList<>();
	private static ClickType clickType;
	private static boolean def;
	private static Properties properties;
	
	private Config() {}

	public static void load() {		
		if (Files.exists(CONFIG_FILE)) {
			loadFromFile();
		} else {
			createFile();
		}

		storeToFile();	//Ensures defaults show
	}

	private static void createFile() {
		try {
			Files.createFile(CONFIG_FILE);
		} catch (IOException e) {
			ClickOpenerMod.LOGGER.error("Failed to create configuration file");
			e.printStackTrace();
		}
		properties = new Properties();
	}

	private static void loadFromFile() {
		Properties properties = new Properties();
		try (var in = Files.newInputStream(CONFIG_FILE)) {
			properties.load(in);
		} catch (IOException e) {
			ClickOpenerMod.LOGGER.error("Failed to read configuration file");
			e.printStackTrace();
		}
		Config.properties = properties;
		loadFromProperties();
	}

	private static void loadFromProperties() {
		clickType = ClickType.tryValueOf((String)properties.computeIfAbsent(CLICK_TYPE_KEY, k->ClickType.RIGHT.name()), ClickType.RIGHT);
		def = Boolean.parseBoolean((String)properties.computeIfAbsent(DEFAULT_KEY, k->"false"));

	}

	private static void readBlocks() {
		NON_DEFAULT_BLOCK_ITEMS.clear();
		properties.forEach((key, value)->{
			if (!(key instanceof String k) || !(value instanceof String v) || CLICK_TYPE_KEY.equals(key) || DEFAULT_KEY.equals(key)) return;

			addItem(new Identifier(k), Boolean.parseBoolean(v), false);
		});
	}

	public static void storeToFile() {
		try (var out = Files.newOutputStream(CONFIG_FILE)) {
			properties.store(out, "Configuration file for Click Opener Mod");
		} catch (IOException e) {
			ClickOpenerMod.LOGGER.error("Failed to write configuration file");
			e.printStackTrace();
		}
	}

	public static void setDefault(boolean def) {
		if (Config.def == def) return;

		Config.def = def;
		properties.setProperty(DEFAULT_KEY, Boolean.toString(def));
		readBlocks();
		storeToFile();
	}

	public static void setClickType(ClickType type) {
		if (clickType == type) return;

		clickType = type;
		properties.setProperty(CLICK_TYPE_KEY, clickType.name());
		storeToFile();
	}

	public static ClickType getClickType() {
		return clickType;
	}

	private static void addItem(Identifier item, boolean allowed, boolean storeToFile) {
		properties.setProperty(item.toString(), Boolean.toString(allowed));
		if (def != allowed) NON_DEFAULT_BLOCK_ITEMS.add(item);
		if (storeToFile) storeToFile();
	}

	public static void addItem(Identifier item, boolean allowed) {
		addItem(item, allowed, true);
	}

	/**
	 * Allows nulls
	 */
	public static boolean isClickTypeAllowed(ClickType clickType) {
		return clickType==null || Config.clickType.equals(clickType);
	}

	public static boolean isBlockItemAllowed(Identifier id) {
		return def != NON_DEFAULT_BLOCK_ITEMS.contains(id);
	}
}
