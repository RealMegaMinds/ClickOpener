package megaminds.clickopener;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ClickType;

@SuppressWarnings({"java:S1104","java:S1192"})
public class Config {
	private static final String WRONG_CLICK_ERROR = "Configuration option 'clickType' must be " + ClickType.LEFT.name() + " or " + ClickType.RIGHT.name()+".";
	@SuppressWarnings("java:S3008")
	private static Config INSTANCE = new Config(new Properties());

	public final ClickType clickType;
	public final boolean smithingTable;
	public final boolean craftingTable;
	public final boolean grindStone;
	public final boolean stoneCutter;
	public final boolean cartographyTable;
	public final boolean loom;
	public final boolean enderChest;
	public final boolean shulkerBox;
	public final boolean enchantingTable;
	public final boolean anvil;
	public final boolean chippedAnvil;
	public final boolean damagedAnvil;

	private Config(Properties properties) {
		var prop = properties.getProperty("clickType");
		if (ClickType.LEFT.name().equals(prop)) {
			clickType = ClickType.LEFT;
		} else if (ClickType.RIGHT.name().equals(prop)) {
			clickType = ClickType.RIGHT;
		} else {
			ClickOpenerMod.LOGGER.warn(WRONG_CLICK_ERROR);
			clickType = ClickType.RIGHT;
		}

		smithingTable = getOrElse(properties.getProperty("smithingTable"), false);
		craftingTable = getOrElse(properties.getProperty("craftingTable"), false);
		grindStone = getOrElse(properties.getProperty("grindStone"), false);
		stoneCutter = getOrElse(properties.getProperty("stoneCutter"), false);
		cartographyTable = getOrElse(properties.getProperty("cartographyTable"), false);
		loom = getOrElse(properties.getProperty("loom"), false);
		enderChest = getOrElse(properties.getProperty("enderChest"), true);
		shulkerBox = getOrElse(properties.getProperty("shulkerBox"), true);
		enchantingTable = getOrElse(properties.getProperty("enchantingTable"), false);
		anvil = getOrElse(properties.getProperty("anvil"), false);
		chippedAnvil = getOrElse(properties.getProperty("chippedAnvil"), false);
		damagedAnvil = getOrElse(properties.getProperty("damagedAnvil"), false);
	}

	private static boolean getOrElse(String bool, boolean def) {
		return switch(Objects.requireNonNullElse(bool, "")) {
		case "true" -> true;
		case "false" -> false;
		default -> def;
		};
	}

	public static Config getInstance() {
		return INSTANCE;
	}

	public static void load() {
		Properties properties = new Properties();
		var configPath = FabricLoader.getInstance().getConfigDir().resolve(ClickOpenerMod.MODID+".properties");

		if(!Files.exists(configPath)) {
			try {
				Files.createFile(configPath);	//Create the file if it doesn't already exist
			} catch (IOException e) {
				ClickOpenerMod.LOGGER.error("Failed to create configuration file!");
				e.printStackTrace();
				return;
			}
		} else {
			try (var in = Files.newInputStream(configPath)) {
				properties.load(in);	//Read the properties from the file if it does exist
			} catch (IOException e) {
				ClickOpenerMod.LOGGER.error("Failed to read configuration file!");
				e.printStackTrace();
				return;
			}
		}

		INSTANCE = new Config(properties);	//Create the config based on the file

		INSTANCE.writeTo(properties);
		try (var out = Files.newOutputStream(configPath)) {
			properties.store(out, "Configuration file for Click Opener Mod");
		} catch (IOException e) {
			ClickOpenerMod.LOGGER.error("Failed to write to configuration file!");
			e.printStackTrace();
		}
	}

	private Properties writeTo(Properties properties) {
		properties.setProperty("clickType", clickType.name());
		properties.setProperty("smithingTable", Boolean.toString(smithingTable));
		properties.setProperty("craftingTable", Boolean.toString(craftingTable));
		properties.setProperty("grindStone", Boolean.toString(grindStone));
		properties.setProperty("stoneCutter", Boolean.toString(stoneCutter));
		properties.setProperty("cartographyTable", Boolean.toString(cartographyTable));
		properties.setProperty("loom", Boolean.toString(loom));
		properties.setProperty("enderChest", Boolean.toString(enderChest));
		properties.setProperty("shulkerBox", Boolean.toString(shulkerBox));
		properties.setProperty("enchantingTable", Boolean.toString(enchantingTable));
		properties.setProperty("anvil", Boolean.toString(anvil));
		properties.setProperty("chippedAnvil", Boolean.toString(chippedAnvil));
		properties.setProperty("damagedAnvil", Boolean.toString(damagedAnvil));
		return properties;
	}

	public boolean isAllowed(String name) {
		return switch (name) {
		case "smithing_table" -> smithingTable;
		case "crafting_table" -> craftingTable;
		case "grindstone" -> grindStone;
		case "stonecutter" -> stoneCutter;
		case "cartography_table" -> cartographyTable;
		case "loom" -> loom;
		case "ender_chest" -> enderChest;
		case "shulker_box" -> shulkerBox;
		case "enchanting_table" -> enchantingTable;
		case "anvil" -> anvil;
		case "chipped_anvil" -> chippedAnvil;
		case "damaged_anvil" -> damagedAnvil;
		default -> false;
		};
	}
}