package megaminds.clickopener.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import megaminds.clickopener.impl.Openable;
import megaminds.clickopener.impl.OpenerHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements OpenerHolder {
	@Unique
	@SuppressWarnings("java:S116")
	private Supplier<ItemStack> clickopener$openStack;
	private List<Runnable> closeListeners;

	@Override
	public void clickopener$setOpenStack(Supplier<ItemStack> stack) {
		clickopener$openStack = stack;
	}

	@Override
	public boolean clickopener$hasOpenStack() {
		return clickopener$openStack != null;
	}

	@SuppressWarnings("unused")
	@Inject(at = @At("RETURN"), method = "onClosed")
	private void clickopener$onClose(PlayerEntity player, CallbackInfo info) {
		if (clickopener$hasOpenStack()) {
			Openable.cast(clickopener$openStack.get()).clickopener$clearCloser();
			clickopener$openStack = null;
		}
		if (closeListeners != null) {
			closeListeners.forEach(Runnable::run);
		}
	}

	@Override
	public void clickopener$addCloseListener(Runnable listener) {
		if (closeListeners == null) closeListeners = new ArrayList<>();
		closeListeners.add(listener);
	}
}
