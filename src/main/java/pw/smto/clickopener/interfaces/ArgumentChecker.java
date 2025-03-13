package pw.smto.clickopener.interfaces;

import com.mojang.brigadier.context.CommandContext;

public interface ArgumentChecker {
	boolean hasArgument(String name);

	public static boolean hasArgument(CommandContext<?> context, String name) {
		return ((ArgumentChecker)context).hasArgument(name);
	}
}
