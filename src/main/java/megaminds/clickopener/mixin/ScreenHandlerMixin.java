package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import megaminds.clickopener.impl.Openable;
import megaminds.clickopener.impl.StackHolder;
import megaminds.clickopener.impl.UseAllower;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements StackHolder, UseAllower {
	@Unique
	@SuppressWarnings("java:S116")
	private ItemStack clickopener$openStack;

	@Unique
	@SuppressWarnings("java:S116")
	private boolean clickopener$isAllowed;

	@Override
	public void clickopener$setOpenStack(ItemStack stack) {
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
			((Openable)(Object)clickopener$openStack).clickopener$clearCloser();
			clickopener$openStack = null;
		}
	}

	@SuppressWarnings("unused")
	@Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
	private static void clickopener$onCanUse(ScreenHandlerContext context, PlayerEntity player, Block block, CallbackInfoReturnable<Boolean> info) {
		if (((UseAllower)player.currentScreenHandler).clickopener$isUseAllowed()) info.setReturnValue(true);
	}

	@Override
	public void clickopener$allowUse() {
		clickopener$isAllowed = true;
	}

	@Override
	public boolean clickopener$isUseAllowed() {
		return clickopener$isAllowed;
	}
}
