package megaminds.clickopener;

import megaminds.clickopener.mixin.AnvilScreenHandlerInvoker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldEvents;

public class AnvilScreenHandler2 extends AnvilScreenHandler {
	private final InventoryLink link;

	public AnvilScreenHandler2(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, Inventory link, int slot) {
		super(syncId, playerInventory, context);
		this.link = new InventoryLink(link, slot);
	}

	@Override
	protected void onTakeOutput(PlayerEntity player, ItemStack stack) {	//NOSONAR - Tries to stay as close to Vanilla as possible
		var self = (AnvilScreenHandlerInvoker)this;

		if (!player.getAbilities().creativeMode) {
			player.addExperienceLevels(-self.getLevelCost().get());
		}
		this.input.setStack(0, ItemStack.EMPTY);
		if (self.getRepairItemUsage() > 0) {
			ItemStack itemStack = this.input.getStack(1);
			if (!itemStack.isEmpty() && itemStack.getCount() > self.getRepairItemUsage()) {
				itemStack.decrement(self.getRepairItemUsage());
				this.input.setStack(1, itemStack);
			} else {
				this.input.setStack(1, ItemStack.EMPTY);
			}
		} else {
			this.input.setStack(1, ItemStack.EMPTY);
		}
		self.getLevelCost().set(0);
		this.context.run((world, pos) -> {
			if (!player.getAbilities().creativeMode && player.getRandom().nextFloat() < 0.12f) {
				var next = getNextAnvilState(link.getStack().getItem());
				link.getStack().decrement(1);
				if (next==null) {
					world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
					((ServerPlayerEntity)player).closeHandledScreen();
					return;
				} else {
					var nextStack = next.getDefaultStack();
					if (!link.addStack(nextStack)) {
						player.getInventory().offerOrDrop(nextStack);
					}

					if (!Config.getInstance().isAllowed(Registry.ITEM.getId(next).getPath())) {
						((ServerPlayerEntity)player).closeHandledScreen();
					}
				}
			}
			world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
		});
	}

	private static Item getNextAnvilState(Item current) {
		if (current==Items.ANVIL) {
			return Items.CHIPPED_ANVIL;
		} else if (current==Items.CHIPPED_ANVIL) {
			return Items.DAMAGED_ANVIL;
		}
		return null;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	private record InventoryLink(Inventory link, int slot) {
		ItemStack getStack() {
			return link.getStack(slot);
		}

		boolean addStack(ItemStack stack) {
			if (getStack().isEmpty() && link.isValid(slot, stack)) {
				link.setStack(slot, stack);
			} else {
				InventoryHelper.addStackToInventory(stack, link);
				if (!stack.isEmpty()) return false;
			}
			return true;
		}
	}
}