package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import megaminds.clickopener.impl.Openable;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements Openable {
	@Unique
	private boolean open;

	@Override
	public void clickopener_open() {
		open = true;
	}

	@Override
	public boolean clickopener_isOpen() {
		return open;
	}

	@Override
	public void clickopener_close() {
		open = false;
	}
}
