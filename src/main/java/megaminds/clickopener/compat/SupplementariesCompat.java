//package megaminds.clickopener.compat;
//
//import java.util.function.BiConsumer;
//import java.util.function.Supplier;
//
//import megaminds.clickopener.ClickOpenerMod;
//import megaminds.clickopener.api.ItemScreenOpener;
//import megaminds.clickopener.api.BlockEntityInventory;
//import megaminds.clickopener.util.ScreenHelper;
//import megaminds.clickopener.api.ItemScreenOpener.ScreenFactoryOpener;
//import net.fabricmc.loader.api.FabricLoader;
//import net.minecraft.entity.mob.PiglinBrain;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.inventory.Inventory;
//import net.minecraft.item.BlockItem;
//import net.minecraft.item.ItemStack;
//import net.minecraft.registry.Registries;
//import net.minecraft.screen.NamedScreenHandlerFactory;
//import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.sound.SoundCategory;
//import net.minecraft.text.Text;
//import net.minecraft.util.DyeColor;
//import net.minecraft.util.Identifier;
//import net.minecraft.world.World;
//
//public class SupplementariesCompat {
//	private SupplementariesCompat() {}
//
//	public static void register(BiConsumer<BlockItem, ItemScreenOpener> registryFunc) {
//		var squaredLoaded = FabricLoader.getInstance().isModLoaded("suppsquared");
//
//		var sackOpener = ItemScreenOpener.requireSingleStack(new ScreenFactoryOpener() {
//			@Override
//			public NamedScreenHandlerFactory createFactory(ItemStack stack, ServerPlayerEntity player, Inventory i) {
//				return new SimpleNamedScreenHandlerFactory(ScreenHelper.adjustableSizeFactoryFor(p -> new BlockEntityInventory(stack, (int)getConfigOption("SACK_SLOTS"), Registries.BLOCK_ENTITY_TYPE.get(new Identifier("supplementaries", "sack"))) {
//					@Override
//					public boolean isValid(int slot, ItemStack stack) {
//						return slot < size() && isAllowedInShulker(stack, player.getServerWorld());
//					}
//
//					@Override
//					@SuppressWarnings("resource")
//					public void onOpen(PlayerEntity player) {
//						var dx = player.getBlockX() + .5;
//						var dy = player.getBlockY() + 1;
//						var dz = player.getBlockZ() + .5;
//						player.getWorld().playSound(null, dx, dy, dz, Registries.SOUND_EVENT.get(new Identifier("supplementaries", "block.sack.open")), SoundCategory.BLOCKS, 1, player.getEntityWorld().random.nextFloat() * .1f + .95f);
//					}
//
//					@Override
//					@SuppressWarnings("resource")
//					public void onClose(PlayerEntity player) {
//						super.onClose(player);
//
//						var dx = player.getBlockX() + .5;
//						var dy = player.getBlockY() + 1;
//						var dz = player.getBlockZ() + .5;
//						player.getWorld().playSound(null, dx, dy, dz, Registries.SOUND_EVENT.get(new Identifier("supplementaries", "block.sack.open")), SoundCategory.BLOCKS, 1, player.getEntityWorld().random.nextFloat() * .1f + .8f);
//					}
//				}, true), stack.hasCustomName() ? stack.getName() : Text.translatable("block.supplementaries.sack"));
//			}
//
//			@Override
//			public void afterSuccess(ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
//				PiglinBrain.onGuardedBlockInteracted(player, true);
//			}
//		});
//
//		registryFunc.accept((BlockItem) Registries.ITEM.get(new Identifier("supplementaries", "sack")), sackOpener);
//
//		//TODO Replace with list from the mod when possible
//		if (squaredLoaded) {
//			for (var color : DyeColor.values()) {
//				registryFunc.accept((BlockItem) Registries.ITEM.get(new Identifier("suppsquared", "sack_"+color.getName())), sackOpener);
//			}
//
//			ClickOpenerMod.LOGGER.info("Supplementaries Squared Compat Loaded");
//		}
//
//		//TODO		registryFunc.accept((BlockItem) Registries.ITEM.get(new Identifier("supplementaries", "safe")), null);
//
//		ClickOpenerMod.LOGGER.info("Supplementaries Compat Loaded");
//	}
//
//	//TODO remove reflection when possible
//	public static Object getConfigOption(String name) {
//		try {
//			return ((Supplier<?>) Class.forName("net.mehvahdjukaar.supplementaries.configs.CommonConfigs$Functional").getDeclaredField(name).get(null)).get();
//		} catch (ReflectiveOperationException e) {
//			throw new RuntimeException("Unable to get Supplementaries config option: "+name);
//		}
//	}
//
//	//TODO remove reflection when possible
//	private static boolean isAllowedInShulker(ItemStack stack, ServerWorld world) {
//		try {
//			return (Boolean) Class.forName("net.mehvahdjukaar.supplementaries.common.utils.MiscUtils").getDeclaredMethod("isAllowedInShulker", ItemStack.class, World.class).invoke(null, stack, world);
//		} catch (ReflectiveOperationException | NullPointerException e) {
//			throw new RuntimeException("Unable to get Supplementaries allowed items.");
//		}
//	}
//}
