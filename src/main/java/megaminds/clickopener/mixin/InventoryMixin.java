package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;

import megaminds.clickopener.impl.DelegateSlotReceiver;
import net.minecraft.inventory.Inventory;

@Mixin(Inventory.class)
@SuppressWarnings("java:S116")
public abstract class InventoryMixin implements DelegateSlotReceiver {
	private boolean clickopener$acceptDelegateSlots;

	@Override
	public boolean clickopener$isAcceptDelegateSlots() {
		return clickopener$acceptDelegateSlots;
	}

	@Override
	public void clickopener$setAcceptDelegateSlots(boolean acceptDelegateSlots) {
		clickopener$acceptDelegateSlots = acceptDelegateSlots;
	}
}
