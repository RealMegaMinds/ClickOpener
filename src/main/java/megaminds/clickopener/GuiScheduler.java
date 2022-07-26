package megaminds.clickopener;

import net.minecraft.screen.NamedScreenHandlerFactory;

public interface GuiScheduler {
	void scheduleOpenGui(NamedScreenHandlerFactory factory);
}