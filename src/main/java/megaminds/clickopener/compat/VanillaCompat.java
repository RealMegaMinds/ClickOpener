package megaminds.clickopener.compat;

import java.util.function.BiConsumer;

import megaminds.clickopener.api.ItemScreenOpener;
import megaminds.clickopener.api.ShulkerInventory;
import megaminds.clickopener.api.ItemScreenOpener.ScreenFactoryOpener;
import megaminds.clickopener.screenhandlers.AnvilItemScreenHandler;
import megaminds.clickopener.util.ScreenHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class VanillaCompat {
	private VanillaCompat() {}

	public static void register(BiConsumer<BlockItem, ItemScreenOpener> registryFunc) {
		registryFunc.accept((BlockItem)Items.ENDER_CHEST, new ScreenFactoryOpener() {
			@Override
			public NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
				return new SimpleNamedScreenHandlerFactory(ScreenHelper.genericScreenHandlerFactoryFor(PlayerEntity::getEnderChestInventory), Text.translatable("container.enderchest"));
			}

			@Override
			public void afterSuccess(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
				player.incrementStat(Stats.OPEN_ENDERCHEST);
				PiglinBrain.onGuardedBlockInteracted(player, true);
			}
		});
		registryFunc.accept((BlockItem)Items.ENCHANTING_TABLE, (ScreenFactoryOpener)(stack, p, i) -> new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new EnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())), stack.getName()));

		registryFunc.accept((BlockItem)Items.CARTOGRAPHY_TABLE, ItemScreenOpener.BLOCK_USE_HANDLER);
		registryFunc.accept((BlockItem)Items.CRAFTING_TABLE, ItemScreenOpener.BLOCK_USE_HANDLER);
		registryFunc.accept((BlockItem)Items.GRINDSTONE, ItemScreenOpener.BLOCK_USE_HANDLER);
		registryFunc.accept((BlockItem)Items.LOOM, ItemScreenOpener.BLOCK_USE_HANDLER);
		registryFunc.accept((BlockItem)Items.SMITHING_TABLE, ItemScreenOpener.BLOCK_USE_HANDLER);
		registryFunc.accept((BlockItem)Items.STONECUTTER, ItemScreenOpener.BLOCK_USE_HANDLER);

		var anvilHandler = new ScreenFactoryOpener() {
			@Override
			public NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity p, Inventory inv) {
				return new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new AnvilItemScreenHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()), inv, stack), Text.translatable("container.repair"));
			}

			@Override
			public void afterSuccess(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
				player.incrementStat(Stats.INTERACT_WITH_ANVIL);
			}
		};
		registryFunc.accept((BlockItem)Items.ANVIL, anvilHandler);
		registryFunc.accept((BlockItem)Items.CHIPPED_ANVIL, anvilHandler);
		registryFunc.accept((BlockItem)Items.DAMAGED_ANVIL, anvilHandler);

		var shulkerBoxHandler = ItemScreenOpener.requireSingleStack(new ScreenFactoryOpener() {
			@Override
			public NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity p, Inventory i) {
				return new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->new ShulkerBoxScreenHandler(syncId, inventory, new ShulkerInventory(stack, 27, BlockEntityType.SHULKER_BOX)), stack.hasCustomName() ? stack.getName() : Text.translatable("container.shulkerBox"));
			}

			@Override
			public void afterSuccess(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
				player.incrementStat(Stats.OPEN_SHULKER_BOX);
				PiglinBrain.onGuardedBlockInteracted(player, true);
			}
		});
		registryFunc.accept((BlockItem)Items.SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.BLACK_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.BLUE_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.BROWN_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.CYAN_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.GRAY_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.GREEN_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.LIGHT_BLUE_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.LIGHT_GRAY_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.LIME_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.MAGENTA_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.ORANGE_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.PINK_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.PURPLE_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.RED_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.WHITE_SHULKER_BOX, shulkerBoxHandler);
		registryFunc.accept((BlockItem)Items.YELLOW_SHULKER_BOX, shulkerBoxHandler);
	}
}
