package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import megaminds.clickopener.impl.DelegateSlotReceiver;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@Mixin(Slot.class)
public abstract class SlotMixin {
	@Shadow
	@Final
	private Inventory inventory;

	@Shadow
	@Final
	private int index;

	@Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (((DelegateSlotReceiver)inventory).clickopener$isAcceptDelegateSlots()) {
			info.setReturnValue(inventory.isValid(index, stack));
		}
	}
}
