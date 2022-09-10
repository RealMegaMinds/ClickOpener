package megaminds.clickopener.api;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ItemScreenHandler {
	NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory slot);
}
