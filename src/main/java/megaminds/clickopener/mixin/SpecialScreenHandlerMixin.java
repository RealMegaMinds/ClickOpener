package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import megaminds.clickopener.impl.UseAllower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

@Pseudo
@Mixin(value = ForgingScreenHandler.class, targets = "net.additionz.misc.FletchingScreenHandler")
public abstract class SpecialScreenHandlerMixin extends ScreenHandler implements UseAllower {
	protected SpecialScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
		super(type, syncId);
	}

	@SuppressWarnings("unused")
	@Inject(method = "canUse(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
	public void clickOpener$onCanUse(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		if (clickopener$isUseAllowed()) info.setReturnValue(true);
	}
}
