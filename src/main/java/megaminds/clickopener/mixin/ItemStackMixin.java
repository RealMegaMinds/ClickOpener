package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import megaminds.clickopener.impl.Openable;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements Openable {
	@Shadow
	public abstract boolean isEmpty();

	@Unique
	private Runnable closer;

	@SuppressWarnings("unused")
	@Inject(at = @At("RETURN"), method = "setCount")
	private void clickopener_onSetCount(int count, CallbackInfo info) {
		if (isEmpty() && closer!=null) {
			var tmp = closer;
			closer = null;
			tmp.run();
		}
	}

	@Override
	public void clickopener_open(Runnable closer) {
		this.closer = closer;
	}

	@Override
	public boolean clickopener_isOpen() {
		return closer != null;
	}

	@Override
	public void clickopener_close() {
		closer = null;
	}
}
