package megaminds.clickopener.compat;

import java.util.function.BiConsumer;

import megaminds.clickopener.api.ItemScreenOpener;
import megaminds.clickopener.api.ItemScreenOpener.ScreenFactoryOpener;
import megaminds.clickopener.util.ScreenHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public class VanillaCompat {
	private VanillaCompat() {}

	public static void register(BiConsumer<BlockItem, ItemScreenOpener> registryFunc) {
		registryFunc.accept((BlockItem)Items.ENDER_CHEST, (ScreenFactoryOpener)(s, p, i) -> new SimpleNamedScreenHandlerFactory(ScreenHelper.genericScreenHandlerFactoryFor(PlayerEntity::getEnderChestInventory), Text.translatable("container.enderchest")));

		registryFunc.accept((BlockItem)Items.ENCHANTING_TABLE, (ScreenFactoryOpener)(stack, p, i) -> new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new EnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())), stack.getName()));

		registryFunc.accept((BlockItem)Items.CARTOGRAPHY_TABLE, ItemScreenOpener.BLOCK_STATE_HANDLER);
		registryFunc.accept((BlockItem)Items.CRAFTING_TABLE, ItemScreenOpener.BLOCK_STATE_HANDLER);
		registryFunc.accept((BlockItem)Items.GRINDSTONE, ItemScreenOpener.BLOCK_STATE_HANDLER);
		registryFunc.accept((BlockItem)Items.LOOM, ItemScreenOpener.BLOCK_STATE_HANDLER);
		registryFunc.accept((BlockItem)Items.SMITHING_TABLE, ItemScreenOpener.BLOCK_STATE_HANDLER);
		registryFunc.accept((BlockItem)Items.STONECUTTER, ItemScreenOpener.BLOCK_STATE_HANDLER);

		registryFunc.accept((BlockItem)Items.ANVIL, ItemScreenOpener.ANVIL_HANDLER);
		registryFunc.accept((BlockItem)Items.CHIPPED_ANVIL, ItemScreenOpener.ANVIL_HANDLER);
		registryFunc.accept((BlockItem)Items.DAMAGED_ANVIL, ItemScreenOpener.ANVIL_HANDLER);

		registryFunc.accept((BlockItem)Items.SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.BLACK_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.BLUE_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.BROWN_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.CYAN_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.GRAY_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.GREEN_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.LIGHT_BLUE_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.LIGHT_GRAY_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.LIME_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.MAGENTA_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.ORANGE_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.PINK_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.PURPLE_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.RED_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.WHITE_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
		registryFunc.accept((BlockItem)Items.YELLOW_SHULKER_BOX, ItemScreenOpener.SHULKER_BOX_HANDLER);
	}
}
