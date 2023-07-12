package megaminds.clickopener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import megaminds.clickopener.api.ClickType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerConfigs {
	public static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(ClickOpenerMod.MODID+"_player.json");
	private static final PlayerConfig PLAYER_DEFAULT = new PlayerConfig(ClickType.RIGHT);

	private final Map<UUID, PlayerConfig> configs;

	public PlayerConfigs() {
		this.configs = new HashMap<>(0);
	}

	private PlayerConfig getOrDefault(UUID uuid) {
		return configs.computeIfAbsent(uuid, u -> PLAYER_DEFAULT.copy());
	}

	public boolean isClickTypeAllowed(ServerPlayerEntity player, ClickType clickType) {
		return clickType == null || clickType.equals(getOrDefault(player.getUuid()).clickType);
	}

	public void setClickType(ServerPlayerEntity player, ClickType clickType) {
		setClickType(player, clickType, true);
	}

	public void setClickType(ServerPlayerEntity player, ClickType clickType, boolean writeToFile) {
		getOrDefault(player.getUuid()).clickType = clickType;
		if (writeToFile) write();
	}

	public void reload() {
		if (Files.exists(CONFIG_FILE)) {
			if (!read()) {
				//Don't write. Allow the user a chance to recover.
				return;
			}
		} else {
			configs.clear();
		}
		write();
	}

	public boolean read() {
		try (var reader = Files.newBufferedReader(CONFIG_FILE)) {
			Map<UUID, PlayerConfig> readIn = ClickOpenerMod.GSON.fromJson(reader, new TypeToken<HashMap<UUID, PlayerConfig>>() {}.getType());
			configs.clear();
			configs.putAll(readIn);
			return true;
		} catch (IOException | JsonParseException e) {
			ClickOpenerMod.LOGGER.error("Failed to read configuration file: {}", e.getMessage());
		}
		return false;
	}

	public void write() {
		try (var out = Files.newBufferedWriter(CONFIG_FILE)) {
			//Uses a copy without defaults to save on file size
			ClickOpenerMod.GSON.toJson(copyWithoutDefaults(configs), out);
		} catch (IOException | JsonIOException e) {
			ClickOpenerMod.LOGGER.error("Failed to write configuration file: {}", e.getMessage());
		}
	}

	private static Map<UUID, PlayerConfig> copyWithoutDefaults(Map<UUID, PlayerConfig> map) {
		var result = new HashMap<UUID, PlayerConfig>(map.size());
		map.forEach((k, v) -> {
			if (!PLAYER_DEFAULT.equals(v)) {
				result.put(k, v);
			}
		});
		return result;
	}

	private static class PlayerConfig {
		private ClickType clickType;

		public PlayerConfig(ClickType clickType) {
			this.clickType = clickType;
		}

		public PlayerConfig copy() {
			return new PlayerConfig(clickType);
		}

		@Override
		public int hashCode() {
			return Objects.hash(clickType);
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj || obj instanceof PlayerConfig other && clickType == other.clickType;
		}
	}
}
