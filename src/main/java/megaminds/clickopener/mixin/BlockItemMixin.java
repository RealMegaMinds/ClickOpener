package megaminds.clickopener.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.spongepowered.asm.mixin.Mixin;

import megaminds.clickopener.Config;
import megaminds.clickopener.GuiScheduler;
import megaminds.clickopener.screenhandlers.AnvilScreenHandler2;
import megaminds.clickopener.screenhandlers.ShulkerInventory;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
	private static final Map<String, BiFunction<World, BlockPos, NamedScreenHandlerFactory>> EASY_MAP = Map.of(
			"smithing_table", getFactoryCreaterFunc(SmithingScreenHandler::new, new TranslatableText("container.upgrade")),
			"crafting_table", getFactoryCreaterFunc(CraftingScreenHandler::new, new TranslatableText("container.crafting")),
			"grindstone", getFactoryCreaterFunc(GrindstoneScreenHandler::new, new TranslatableText("container.grindstone_title")),
			"stonecutter", getFactoryCreaterFunc(StonecutterScreenHandler::new, new TranslatableText("container.stonecutter")),
			"cartography_table", getFactoryCreaterFunc(CartographyTableScreenHandler::new, new TranslatableText("container.cartography_table")),
			"loom", getFactoryCreaterFunc(LoomScreenHandler::new, new TranslatableText("container.loom"))
			);

	protected BlockItemMixin(Settings settings) {
		super(settings);
	}

	private static BiFunction<World, BlockPos, NamedScreenHandlerFactory> getFactoryCreaterFunc(ScreenHandlerCreatorFunction handlerCreator, Text title) {
		return (world, pos) -> new SimpleNamedScreenHandlerFactory((syncId, inventory, player2)->handlerCreator.apply(syncId, inventory, getContext(world, pos)), title);
	}

	private static interface ScreenHandlerCreatorFunction {
		ScreenHandler apply(int syncId, PlayerInventory inventory, ScreenHandlerContext context);
	}

	private static ScreenHandlerContext getContext(World world, BlockPos pos) {
		return new ScreenHandlerContext() {
			@Override
			public <T> Optional<T> get(BiFunction<World, BlockPos, T> getter) {
				getter.apply(world, pos);
				return Optional.empty();
			};
		};
	}

	@Override
	public boolean onClicked(ItemStack clickedStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		String name;
		if (!player.world.isClient && Config.getInstance().clickType==clickType && Config.getInstance().isAllowed((name=Registry.ITEM.getId(this).getPath()))) {
			var p = (ServerPlayerEntity) player;
			var blockState = Block.getBlockFromItem(this).getDefaultState();			

			NamedScreenHandlerFactory factory = null;
			if (this == Items.ENDER_CHEST) {
				factory = new SimpleNamedScreenHandlerFactory((syncId, inventory, player2)->GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player2.getEnderChestInventory()), new TranslatableText("container.enderchest"));
			} else if (this == Items.ENCHANTING_TABLE) {
				factory = new SimpleNamedScreenHandlerFactory((syncId, inventory, player2)->new EnchantmentScreenHandler(syncId, inventory, getContext(p.getWorld(), p.getBlockPos())), clickedStack.getName());
			} else if (EASY_MAP.keySet().contains(name)) {
				factory = EASY_MAP.get(name).apply(p.getWorld(), p.getBlockPos());
			} else if (blockState.isIn(BlockTags.SHULKER_BOXES)) {
				factory = new ShulkerInventory(clickedStack);
			} else if (blockState.isIn(BlockTags.ANVIL)) {
				factory = new SimpleNamedScreenHandlerFactory((syncId, inventory, player2)->new AnvilScreenHandler2(syncId, inventory, getContext(p.getWorld(), p.getBlockPos()), slot.inventory, slot.getIndex()), new TranslatableText("container.repair"));
			}

			if (factory != null) {
				((GuiScheduler)p.networkHandler).scheduleOpenGui(factory);
				return true;
			}
		}

		return super.onClicked(clickedStack, cursorStack, slot, clickType, player, cursorStackReference);
	}
}