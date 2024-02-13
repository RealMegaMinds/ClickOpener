package megaminds.clickopener.impl;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

public interface OpenerHolder {
	void clickopener$setOpenStack(Supplier<ItemStack> stack);
	boolean clickopener$hasOpenStack();
	void clickopener$addCloseListener(Runnable listener);
}
