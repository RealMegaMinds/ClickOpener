package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
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
	private ItemStack openedStack;

	@SuppressWarnings("unused")
	@Inject(at = @At("RETURN"), method = "onClosed")
	private void clickopener_onClose(PlayerEntity player, CallbackInfo info) {
		if (openedStack!=null) {
			((Openable)(Object)openedStack).clickopener$clearCloser();
			openedStack = null;
		}
	}

	@Override
	public void clickopener_setStack(ItemStack stack) {
		openedStack = stack;
	}
}
