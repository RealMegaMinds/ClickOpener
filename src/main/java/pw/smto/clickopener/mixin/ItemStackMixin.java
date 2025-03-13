package pw.smto.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pw.smto.clickopener.interfaces.Openable;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements Openable {
	@Shadow
	public abstract boolean isEmpty();

	@Unique
	@SuppressWarnings("java:S116")
	private Runnable clickopener$closer;

	@SuppressWarnings("unused")
	@Inject(at = @At("RETURN"), method = "setCount")
	private void clickopener$onSetCount(int count, CallbackInfo info) {
		if (isEmpty() && clickopener$hasCloser()) {
			var tmp = clickopener$closer;
			clickopener$clearCloser();
			tmp.run();
		}
	}

	@Override
	public void clickopener$setCloser(Runnable closer) {
		this.clickopener$closer = closer;
	}

	@Override
	public Runnable clickopener$getCloser() {
		return clickopener$closer;
	}

	@Override
	public boolean clickopener$hasCloser() {
		return clickopener$closer != null;
	}
}
