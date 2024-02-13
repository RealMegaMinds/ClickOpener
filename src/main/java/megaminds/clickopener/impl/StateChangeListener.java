package megaminds.clickopener.impl;

import megaminds.clickopener.util.OpenContext;
import net.minecraft.block.BlockState;

public interface StateChangeListener {
	void postBlockStateChange(BlockState oldState, OpenContext context);
}
