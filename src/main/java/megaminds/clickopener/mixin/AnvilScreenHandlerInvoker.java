package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;

@Mixin(AnvilScreenHandler.class)
public interface AnvilScreenHandlerInvoker {
	@Accessor
	Property getLevelCost();

	@Accessor
	int getRepairItemUsage();

	@Accessor
	void setRepairItemUsage(int repairItemUsage);
}