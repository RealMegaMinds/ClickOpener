package megaminds.clickopener.screenhandlers;

import it.unimi.dsi.fastutil.Pair;
import megaminds.clickopener.mixin.AnvilScreenHandlerInvoker;
import megaminds.clickopener.util.InventoryHelper;
import megaminds.clickopener.util.ScreenHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.WorldEvents;

public class AnvilItemScreenHandler extends AnvilScreenHandler {
	private final ItemStack link;
	private final Inventory inventory;

	public AnvilItemScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, Inventory inventory, ItemStack link) {
		super(syncId, playerInventory, context);
		this.link = link;
		this.inventory = inventory;
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
			if (!player.getAbilities().creativeMode && player.getRandom().nextFloat() < .12f) {
				var next = getNextAnvilState(link.getItem());
				link.decrement(1);
				if (next==null) {	//Anvil destroyed
					((ServerPlayerEntity)player).closeHandledScreen();
					world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
					return;
				}

				var pair = addToInventory(next);
				if (pair!=null) {	//Anvil fits
					ScreenHelper.openScreen((ServerPlayerEntity)player, null, pair.left(), pair.right());
				} else {
					((ServerPlayerEntity)player).closeHandledScreen();
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

	/**
	 * The returned stack may be different than the given stack if it was combined with another one.
	 */
	private Pair<ItemStack, Inventory> addToInventory(Item item) {
		var stack2 = InventoryHelper.addItemToInventory(item, inventory);
		if (stack2 != null) return Pair.of(stack2, inventory);
		stack2 = InventoryHelper.addItemToInventory(item, player.getInventory());
		if (stack2 != null) return Pair.of(stack2, player.getInventory());
		player.dropItem(item);
		return null;
	}
}