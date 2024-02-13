package megaminds.clickopener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;

import megaminds.clickopener.api.ClickType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * whitelist
 * - Contains ids of items (minecraft:crafting_table), item tags prefixed by item (item#minecraft:anvil),
 *   or block tags prefixed by block (block#minecraft:shulker_boxes). Tags without prefix will add both item and block.
 * 
 * blacklist
 * - Contains ids of items. Useful for excluding a single item from a tag (minecraft:damaged_anvil).
 */
public class Config {
	public static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(ClickOpenerMod.MODID+".json");

	private final Set<TagKey<Item>> itemTagsList;
	private final Set<TagKey<Block>> blockTagsList;
	private final Set<Identifier> idList;
	private final Set<Identifier> blacklist;
	private ClickType clickType;

	public Set<TagKey<Item>> getItemTagsList() {
		return itemTagsList;
	}

	public Set<TagKey<Block>> getBlockTagsList() {
		return blockTagsList;
	}

	public Set<Identifier> getIdList() {
		return idList;
	}

	public Set<Identifier> getBlacklist() {
		return blacklist;
	}

	public ClickType getClickType() {
		return clickType;
	}

	public Config() {
		this.itemTagsList = new HashSet<>();
		this.blockTagsList = new HashSet<>();
		this.idList = new HashSet<>();
		this.blacklist = new HashSet<>();
		this.clickType = ClickType.RIGHT;
	}

	public void reset() {
		this.idList.clear();
		this.itemTagsList.clear();
		this.blockTagsList.clear();
		this.blacklist.clear();
		this.clickType = ClickType.RIGHT;
	}

	public void reload() {
		if (Files.exists(CONFIG_FILE)) {
			if (!read()) {
				//Don't write. Allow the user a chance to recover.
				return;
			}
		} else {
			reset();
		}
		write();
	}

	public boolean read() {
		try (var reader = Files.newBufferedReader(CONFIG_FILE)) {
			ClickOpenerMod.GSON.fromJson(reader, ConfigBuilder.class).fill(this);
			return true;
		} catch (IOException | JsonParseException e) {
			ClickOpenerMod.LOGGER.error("Failed to read configuration file: {}", e.getMessage());
		}
		return false;
	}

	public void write() {
		var builder = new ConfigBuilder(this);
		try (var out = Files.newBufferedWriter(CONFIG_FILE)) {
			ClickOpenerMod.GSON.toJson(builder, out);
		} catch (IOException | JsonIOException e) {
			ClickOpenerMod.LOGGER.error("Failed to write configuration file: {}", e.getMessage());
			ClickOpenerMod.LOGGER.error("Current Contents: {}", builder);
		}
	}

	public void addBlockItem(Identifier id, boolean allow, boolean writeToFile) {
		if (allow) {
			idList.add(id);
		} else {
			blacklist.add(id);
		}
		if (writeToFile) write();
	}

	public void addBlockItem(Identifier id, boolean allow) {
		addBlockItem(id, allow, true);
	}

	public void removeBlockItem(Identifier id, boolean allow) {
		if (allow) {
			idList.remove(id);
		} else {
			blacklist.remove(id);
		}
		write();
	}

	public void addItemTag(Identifier tag) {
		itemTagsList.add(TagKey.of(RegistryKeys.ITEM, tag));
		write();
	}

	public void addBlockTag(Identifier tag) {
		blockTagsList.add(TagKey.of(RegistryKeys.BLOCK, tag));
		write();
	}

	public void removeItemTag(Identifier tag) {
		itemTagsList.remove(TagKey.of(RegistryKeys.ITEM, tag));
		write();
	}

	public void removeBlockTag(Identifier tag) {
		blockTagsList.remove(TagKey.of(RegistryKeys.BLOCK, tag));
		write();
	}

	public void setClickType(ClickType clickType) {
		this.clickType = Objects.requireNonNullElse(clickType, ClickType.RIGHT);
	}

	public boolean isAllowed(BlockItem item) {
		var id = Registries.ITEM.getId(item);
		return (idList.contains(id)
				|| itemTagsList.stream().anyMatch(Registries.ITEM.getEntry(item)::isIn)
				|| blockTagsList.stream().anyMatch(Registries.BLOCK.getEntry(item.getBlock())::isIn))
				&& !blacklist.contains(id);
	}

	private static record ConfigBuilder(Set<String> whitelist, Set<Identifier> blacklist, ClickType defaultClickType) {
		public ConfigBuilder(Config config) {
			this(new HashSet<>(), new HashSet<>(), config.clickType);
			for (var k : config.itemTagsList) {
				whitelist.add("item#"+k.id());
			}
			for (var k : config.blockTagsList) {
				whitelist.add("block#"+k.id());
			}
			for (var b : config.idList) {
				whitelist.add(b.toString());
			}
			blacklist.addAll(config.blacklist);
		}

		public void fill(Config config) {
			config.reset();
			for (var s : whitelist) {
				var arr = s.split("#",2);
				if (arr.length == 1) {
					config.idList.add(new Identifier(s));
				} else if (arr.length == 2) {
					var id = new Identifier(arr[1]);
					if (!arr[0].equals("item")) {
						config.blockTagsList.add(TagKey.of(RegistryKeys.BLOCK, id));
					}
					if (!arr[0].equals("block")) {
						config.itemTagsList.add(TagKey.of(RegistryKeys.ITEM, id));
					}
				}
			}

			config.blacklist.addAll(blacklist);
			config.setClickType(defaultClickType);
		}

		@Override
		public String toString() {
			return "[whitelist="+whitelist+", blacklist="+blacklist+", defaultClickType=" + defaultClickType + "]";
		}
	}
}
