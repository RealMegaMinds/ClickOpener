package megaminds.clickopener.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import megaminds.clickopener.ShulkerInventory;

@Mixin(Item.class)
public abstract class BlockItemMixin {	
	private static final NamedScreenHandlerFactory ENDER_CHEST_HANDLER_FACTORY = new SimpleNamedScreenHandlerFactory((syncId, inventory, player2)->GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player2.getEnderChestInventory()), new TranslatableText("container.enderchest"));

	@SuppressWarnings("deprecation")
	@Inject(method = "onClicked", at = @At(value = "HEAD"), cancellable = true)
	private void checkBlockClicks(ItemStack myStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> info) {
		if (!(((Object)this) instanceof BlockItem b)) return;	//NOSONAR

		if (clickType==ClickType.RIGHT && !player.world.isClient) {
			if (b == Items.ENDER_CHEST) {
				player.openHandledScreen(ENDER_CHEST_HANDLER_FACTORY);
			} else if (b.getBlock().getRegistryEntry().isIn(BlockTags.SHULKER_BOXES)) {
				player.openHandledScreen(new ShulkerInventory(myStack));
			}
			info.setReturnValue(true);
		}
	}
}