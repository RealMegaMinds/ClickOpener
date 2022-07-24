package megaminds.clickopener;

import net.minecraft.util.ClickType;

public class Config {
	public static final ClickType CLICK_TYPE = ClickType.RIGHT;
	public static final boolean REOPEN_INVENTORY_ON_CLOSE = true;
	
	private Config() {}
	
	public static ClickType getClickType() {
		return CLICK_TYPE;
	}
	
	public static boolean isReopenInventoryOnClose() {
		return REOPEN_INVENTORY_ON_CLOSE;
	}
}