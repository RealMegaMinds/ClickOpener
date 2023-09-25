package megaminds.clickopener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
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

	public Config() {
		this.itemTagsList = new HashSet<>();
		this.blockTagsList = new HashSet<>();
		this.idList = new HashSet<>();
		this.blacklist = new HashSet<>();
	}

	public void reset() {
		this.idList.clear();
		this.itemTagsList.clear();
		this.blockTagsList.clear();
		this.blacklist.clear();
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

	public void addBlockItem(Identifier id, boolean whitelist, boolean storeToFile) {
		if (whitelist) {
			idList.add(id);
		} else {
			blacklist.add(id);
		}
		if (storeToFile) write();
	}

	public void addBlockItem(Identifier id, boolean whitelist) {
		addBlockItem(id, whitelist, true);
	}

	public void removeBlockItem(Identifier id, boolean whitelist, boolean storeToFile) {
		if (whitelist) {
			idList.remove(id);
		} else {
			blacklist.remove(id);
		}
		if (storeToFile) write();
	}

	public void removeBlockItem(Identifier id, boolean whitelist) {
		removeBlockItem(id, whitelist, true);
	}

	public void addItemTag(Identifier tag, boolean storeToFile) {
		itemTagsList.add(TagKey.of(RegistryKeys.ITEM, tag));
		if (storeToFile) write();
	}

	public void addItemTag(Identifier tag) {
		addItemTag(tag, true);
	}

	public void addBlockTag(Identifier tag, boolean storeToFile) {
		blockTagsList.add(TagKey.of(RegistryKeys.BLOCK, tag));
		if (storeToFile) write();
	}

	public void addBlockTag(Identifier tag) {
		addBlockTag(tag, true);
	}

	public void removeItemTag(Identifier tag, boolean storeToFile) {
		itemTagsList.remove(TagKey.of(RegistryKeys.ITEM, tag));
		if (storeToFile) write();
	}

	public void removeItemTag(Identifier tag) {
		removeItemTag(tag, true);
	}

	public void removeBlockTag(Identifier tag, boolean storeToFile) {
		blockTagsList.remove(TagKey.of(RegistryKeys.BLOCK, tag));
		if (storeToFile) write();
	}

	public void removeBlockTag(Identifier tag) {
		removeBlockTag(tag, true);
	}

	public boolean isAllowed(BlockItem item) {
		var id = Registries.ITEM.getId(item);
		return (idList.contains(id)
				|| itemTagsList.stream().anyMatch(Registries.ITEM.getEntry(item)::isIn)
				|| blockTagsList.stream().anyMatch(Registries.BLOCK.getEntry(item.getBlock())::isIn))
				&& !blacklist.contains(id);
	}

	private static record ConfigBuilder(Set<String> whitelist, Set<Identifier> blacklist) {
		public ConfigBuilder(Config config) {
			this(new HashSet<>(), new HashSet<>());
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
		}

		@Override
		public String toString() {
			return "[list="+whitelist+", exceptions="+blacklist+"]";
		}
	}
}
