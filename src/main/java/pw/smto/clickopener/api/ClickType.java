package pw.smto.clickopener.api;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.StringIdentifiable;

public enum ClickType implements StringIdentifiable {
	LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, DROP, CTRL_DROP,
	NONE;

	private static final Map<String, ClickType> VALUES = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

	public static ClickType convert(SlotActionType action, int button, int slot) {
		return switch (action) {
			case PICKUP -> button == 0 ? LEFT : RIGHT;
			case QUICK_MOVE -> button == 0 ? SHIFT_LEFT : SHIFT_RIGHT;
			case THROW ->  evalThrow(slot, button);
			default -> NONE;
		};
	}

	private static ClickType evalThrow(int slot, int button) {
		if (slot == -99) return NONE;
		return button == 0 ? DROP : CTRL_DROP;
	}

	public static ClickType tryValueOf(String s) {
		return VALUES.getOrDefault(s, NONE);
	}

	@Override
	public String asString() {
		return name();
	}
}
