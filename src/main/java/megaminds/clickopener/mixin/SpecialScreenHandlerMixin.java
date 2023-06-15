package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import megaminds.clickopener.impl.UseAllower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.StonecutterScreenHandler;

@Mixin({CartographyTableScreenHandler.class, CraftingScreenHandler.class, GrindstoneScreenHandler.class, LoomScreenHandler.class, ForgingScreenHandler.class, StonecutterScreenHandler.class, EnchantmentScreenHandler.class})
public abstract class SpecialScreenHandlerMixin extends ScreenHandler implements UseAllower {
	@Unique
	private boolean allowed;

	protected SpecialScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
		super(type, syncId);
	}

	@SuppressWarnings("unused")
	@Inject(method = "canUse(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
	public void clickOpener_onCanUse(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
		if (allowed) info.setReturnValue(true);
	}

	@Override
	public void clickOpener_allowUse() {
		this.allowed = true;
	}
}
