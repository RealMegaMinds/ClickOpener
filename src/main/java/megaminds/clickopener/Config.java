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
 * - True: List is a Whitelist (only these blocks/items are allowed)
 * - False: List is a Blacklist (all blocks/items except these are allowed)
 * 
 * list
 * - Contains ids of items (minecraft:crafting_table), item tags prefixed by item (item#minecraft:anvil),
 *   or block tags prefixed by block (block#minecraft:shulker_boxes). Tags without prefix will check both item and block.
 * 
 * exceptions
 * - Contains ids of items. Useful for excluding a single item from a tag (minecraft:damaged_anvil).
 */
public class Config {
	public static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(ClickOpenerMod.MODID+".json");

	private boolean whitelist;
	private final Set<TagKey<Item>> itemTagsList;
	private final Set<TagKey<Block>> blockTagsList;
	private final Set<Identifier> idList;
	private final Set<Identifier> exceptions;

	private boolean ignoreUUIDWithoutPlayer;

	public Config() {
		this(true);
	}

	public boolean isWhitelist() {
		return whitelist;
	}

	public Set<TagKey<Item>> getItemTagsList() {
		return itemTagsList;
	}

	public Set<TagKey<Block>> getBlockTagsList() {
		return blockTagsList;
	}

	public Set<Identifier> getIdList() {
		return idList;
	}

	public Set<Identifier> getExceptions() {
		return exceptions;
	}

	public void setWhitelist(boolean whitelist) {
		this.whitelist = whitelist;
	}

	public Config(boolean whitelist) {
		this.whitelist = whitelist;
		this.itemTagsList = new HashSet<>();
		this.blockTagsList = new HashSet<>();
		this.idList = new HashSet<>();
		this.exceptions = new HashSet<>();
	}

	public void reset() {
		reset(true);
	}

	public void reset(boolean whitelist) {
		this.whitelist = whitelist;
		this.idList.clear();
		this.itemTagsList.clear();
		this.blockTagsList.clear();
		this.exceptions.clear();
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

	public void addBlockItem(Identifier id, boolean list, boolean storeToFile) {
		if (list) {
			idList.add(id);
		} else {
			exceptions.add(id);
		}
		if (storeToFile) write();
	}

	public void addBlockItem(Identifier id, boolean list) {
		addBlockItem(id, list, true);
	}

	public void removeBlockItem(Identifier id, boolean list, boolean storeToFile) {
		if (list) {
			idList.remove(id);
		} else {
			exceptions.remove(id);
		}
		if (storeToFile) write();
	}

	public void removeBlockItem(Identifier id, boolean list) {
		removeBlockItem(id, list, true);
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
		var isInLists = idList.contains(id) || itemTagsList.stream().anyMatch(Registries.ITEM.getEntry(item)::isIn) || blockTagsList.stream().anyMatch(Registries.BLOCK.getEntry(item.getBlock())::isIn);
		return whitelist && isInLists && !exceptions.contains(id) || !whitelist && (!isInLists || exceptions.contains(id));
	}

	private static record ConfigBuilder(boolean ignoreUUIDWithoutPlayer, boolean whitelist, Set<String> list, Set<Identifier> exceptions) {
		public ConfigBuilder(Config config) {
			this(config.ignoreUUIDWithoutPlayer, config.whitelist, new HashSet<>(), new HashSet<>());
			for (var k : config.itemTagsList) {
				list.add("item#"+k.id());
			}
			for (var k : config.blockTagsList) {
				list.add("block#"+k.id());
			}
			for (var b : config.idList) {
				list.add(b.toString());
			}
			exceptions.addAll(config.exceptions);
		}

		public void fill(Config config) {
			config.reset(whitelist);
			config.ignoreUUIDWithoutPlayer = ignoreUUIDWithoutPlayer;
			for (var s : list) {
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

			config.exceptions.addAll(exceptions);
		}

		@Override
		public String toString() {
			return "[whitelist="+whitelist+", list="+list+", exceptions="+exceptions+"]";
		}
	}
}
