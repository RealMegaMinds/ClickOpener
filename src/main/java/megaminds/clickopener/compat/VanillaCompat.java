package megaminds.clickopener.compat;

import megaminds.clickopener.api.HandlerRegistry;
import megaminds.clickopener.api.ItemScreenHandler;
import megaminds.clickopener.api.ShulkerInventory;
import megaminds.clickopener.screenhandlers.AnvilItemScreenHandler;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public class VanillaCompat {
	private VanillaCompat() {}

	public static void init() {
		HandlerRegistry.register((BlockItem)Items.ENDER_CHEST, (s,p,i) -> new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player.getEnderChestInventory()), Text.translatable("container.enderchest")));

		HandlerRegistry.register((BlockItem)Items.ENCHANTING_TABLE, (s,p,i) -> new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new EnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())), s.getName()));

		ItemScreenHandler stateHandler = (s,p,i) -> ((BlockItem)s.getItem()).getBlock().getDefaultState().createScreenHandlerFactory(p.getWorld(), p.getBlockPos());
		HandlerRegistry.register((BlockItem)Items.CARTOGRAPHY_TABLE, stateHandler);
		HandlerRegistry.register((BlockItem)Items.CRAFTING_TABLE, stateHandler);
		HandlerRegistry.register((BlockItem)Items.GRINDSTONE, stateHandler);
		HandlerRegistry.register((BlockItem)Items.LOOM, stateHandler);
		HandlerRegistry.register((BlockItem)Items.SMITHING_TABLE, stateHandler);
		HandlerRegistry.register((BlockItem)Items.STONECUTTER, stateHandler);

		ItemScreenHandler anvilHandler = (s,p,i) -> new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new AnvilItemScreenHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()), i, s), Text.translatable("container.repair"));
		HandlerRegistry.register((BlockItem)Items.ANVIL, anvilHandler);
		HandlerRegistry.register((BlockItem)Items.CHIPPED_ANVIL, anvilHandler);
		HandlerRegistry.register((BlockItem)Items.DAMAGED_ANVIL, anvilHandler);

		var shulkerHandler = ItemScreenHandler.requireSingleStack((stack, p, i)->new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new ShulkerBoxScreenHandler(syncId, inventory, new ShulkerInventory(stack, 27, BlockEntityType.SHULKER_BOX)), stack.hasCustomName() ? stack.getName() : Text.translatable("container.shulkerBox")));
		HandlerRegistry.register((BlockItem)Items.SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.BLACK_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.BLUE_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.BROWN_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.CYAN_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.GRAY_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.GREEN_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.LIGHT_BLUE_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.LIGHT_GRAY_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.LIME_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.MAGENTA_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.ORANGE_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.PINK_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.PURPLE_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.RED_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.WHITE_SHULKER_BOX, shulkerHandler);
		HandlerRegistry.register((BlockItem)Items.YELLOW_SHULKER_BOX, shulkerHandler);
	}
}
