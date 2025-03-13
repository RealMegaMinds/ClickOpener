package pw.smto.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pw.smto.clickopener.interfaces.UseAllower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

@Pseudo
@Mixin(targets = "net.additionz.misc.FletchingScreenHandler")
public abstract class ForcedScreenHandlerMixin extends ScreenHandler implements UseAllower {
	@Unique
	@SuppressWarnings("java:S116")
	private boolean clickopener$isAllowed;

	protected ForcedScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
		super(type, syncId);
	}

	@Override
	public void clickopener$allowUse() {
		clickopener$isAllowed = true;
	}

	@Override
	public boolean clickopener$isUseAllowed() {
		return clickopener$isAllowed;
	}

	@SuppressWarnings("unused")
	@Inject(method = "canUse(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
	public void clickOpener$onCanUse(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		if (clickopener$isUseAllowed()) info.setReturnValue(true);
	}
}
