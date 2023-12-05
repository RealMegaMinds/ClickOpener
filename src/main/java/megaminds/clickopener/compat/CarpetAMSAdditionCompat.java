package megaminds.clickopener.compat;

import java.util.function.BiConsumer;

import club.mcams.carpet.AmsServerSettings;
import club.mcams.carpet.screen.largeShulkerBox.largeShulkerBoxScreenHandler;
import megaminds.clickopener.ClickOpenerMod;
import megaminds.clickopener.api.BlockEntityInventory;
import megaminds.clickopener.api.ItemScreenOpener;
import megaminds.clickopener.api.ItemScreenOpener.ScreenFactoryOpener;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CarpetAMSAdditionCompat {
	public static final Identifier CARPET_AMS_ADDITION_PHASE = new Identifier("carpet-ams-addition");
	
	private CarpetAMSAdditionCompat() {}
	
	public static void register(BiConsumer<BlockItem, ItemScreenOpener> registryFunc) {
		var shulkerBoxHandler = ItemScreenOpener.requireSingleStack(new ScreenFactoryOpener() {
			@Override
			public NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity p, Inventory i) {
				return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
					if (AmsServerSettings.largeShulkerBox) {
						return new largeShulkerBoxScreenHandler(syncId, inventory, new BlockEntityInventory(stack, 9 * 6, BlockEntityType.SHULKER_BOX));
					}

					return new ShulkerBoxScreenHandler(syncId, inventory, new BlockEntityInventory(stack, 27, BlockEntityType.SHULKER_BOX));
				}, stack.hasCustomName() ? stack.getName() : Text.translatable("container.shulkerBox"));
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
		
		ClickOpenerMod.LOGGER.info("Carpet AMS Addition Compat Loaded");
	}
}
