package megaminds.clickopener.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ClickType;
import net.minecraft.util.registry.Registry;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;

import megaminds.clickopener.AnvilScreenHandler2;
import megaminds.clickopener.Config;
import megaminds.clickopener.GuiScheduler;
import megaminds.clickopener.screenhandlers.PermissiveNamedScreenHandlerFactory;
import megaminds.clickopener.screenhandlers.ShulkerInventory;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
	private static final Set<String> EASY = Set.of("smithing_table", "crafting_table", "grindstone", "stonecutter", "cartography_table", "loom");

	protected BlockItemMixin(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack clickedStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		if (!player.world.isClient && Config.getInstance().clickType==clickType) {
			var p = (ServerPlayerEntity) player;
			var blockState = Block.getBlockFromItem(this).getDefaultState();			

			NamedScreenHandlerFactory factory = null;
			var name = Registry.ITEM.getId(this).getPath();
			if (this == Items.ENDER_CHEST) {
				factory = new SimpleNamedScreenHandlerFactory((syncId, inventory, player2)->GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player2.getEnderChestInventory()), new TranslatableText("container.enderchest"));
			} else if (this == Items.ENCHANTING_TABLE) {
				factory = new PermissiveNamedScreenHandlerFactory((syncId, inventory, player2)->new EnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(p.getWorld(), p.getBlockPos())), clickedStack.getName());
			} else if (EASY.contains(name)) {
				factory = new PermissiveNamedScreenHandlerFactory(blockState.createScreenHandlerFactory(p.getWorld(), p.getBlockPos()));
			} else if (blockState.isIn(BlockTags.SHULKER_BOXES)) {
				factory = new ShulkerInventory(clickedStack);
			} else if (blockState.isIn(BlockTags.ANVIL)) {
				factory = new SimpleNamedScreenHandlerFactory((syncId, inventory, player2)->new AnvilScreenHandler2(syncId, inventory, ScreenHandlerContext.create(p.getWorld(), p.getBlockPos()), slot.inventory, slot.getIndex()), new TranslatableText("container.repair"));
			}

			if (factory != null && Config.getInstance().isAllowed(name)) {
				((GuiScheduler)p.networkHandler).scheduleOpenGui(factory);
				return true;
			}
		}

		return super.onClicked(clickedStack, cursorStack, slot, clickType, player, cursorStackReference);
	}
}