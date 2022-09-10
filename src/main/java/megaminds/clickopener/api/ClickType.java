package megaminds.clickopener.api;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.StringIdentifiable;

public enum ClickType implements StringIdentifiable {
	LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, DROP, CTRL_DROP,
	OTHER;

	private static final Map<String, ClickType> VALUES = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

	public static ClickType convert(SlotActionType action, int button, int slot) {
		return switch (action) {
		case PICKUP -> button == 0 ? LEFT : RIGHT;
		case QUICK_MOVE -> button == 0 ? SHIFT_LEFT : SHIFT_RIGHT;
		case THROW ->  slot == -999 ? OTHER : (button == 0 ? DROP : CTRL_DROP);
		default -> OTHER;
		};
	}

	public static ClickType tryValueOf(String s, ClickType def) {
		if (s==null) return def;
		return VALUES.getOrDefault(s, def);
	}

	@Override
	public String asString() {
		return name();
	}
}
