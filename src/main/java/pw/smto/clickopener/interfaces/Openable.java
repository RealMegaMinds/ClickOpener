package pw.smto.clickopener.interfaces;

import net.minecraft.item.ItemStack;

public interface Openable {
	static Openable cast(ItemStack stack) {
		return (Openable)(Object)stack;
	}

	Runnable clickopener$getCloser();
	void clickopener$setCloser(Runnable closer);
	boolean clickopener$hasCloser();
	default Runnable clickopener$clearCloser() {
		var closer = clickopener$getCloser();
		clickopener$setCloser(null);
		return closer;
	}
}
