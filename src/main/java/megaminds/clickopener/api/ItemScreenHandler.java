package megaminds.clickopener.api;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ItemScreenHandler {
	NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory);

	@SuppressWarnings("unused")
	default boolean canCreateFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
		return true;
	}

	public static ItemScreenHandler requireSingleStack(ItemScreenHandler handler) {
		return new ItemScreenHandler() {
			@Override
			public NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
				return handler.createFactory(stack, player, inventory);
			}

			@Override
			public boolean canCreateFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
				return stack.getCount() == 1;
			}
		};
	}
}
