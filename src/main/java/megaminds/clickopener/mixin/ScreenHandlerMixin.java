package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import megaminds.clickopener.impl.Openable;
import megaminds.clickopener.impl.StackHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements StackHolder {
	@Unique
	@SuppressWarnings("java:S116")
	private ItemStack clickopener$openStack;

	@SuppressWarnings("unused")
	@Inject(at = @At("RETURN"), method = "onClosed")
	private void clickopener$onClose(PlayerEntity player, CallbackInfo info) {
		if (clickopener$hasOpenStack()) {
			((Openable)(Object)clickopener$openStack).clickopener$clearCloser();
			clickopener$openStack = null;
		}
	}

	@Override
	public void clickopener$setOpenStack(ItemStack stack) {
		clickopener$openStack = stack;
	}

	@Override
	public boolean clickopener$hasOpenStack() {
		return clickopener$openStack != null;
	}
}
