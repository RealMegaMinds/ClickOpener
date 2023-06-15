package megaminds.clickopener.compat;

import java.util.function.BiConsumer;

import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler;
import atonkish.reinfshulker.block.entity.ModBlockEntityType;
import atonkish.reinfshulker.item.ModItems;
import megaminds.clickopener.ClickOpenerMod;
import megaminds.clickopener.api.ItemScreenHandler;
import megaminds.clickopener.api.ShulkerInventory;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

public class ReinforcedShulkersCompat {
	private ReinforcedShulkersCompat() {}

	public static void register(BiConsumer<BlockItem, ItemScreenHandler> registryFunc) {
		ModItems.REINFORCED_SHULKER_BOX_MAP.entrySet().forEach(e->{
			var material = e.getKey();
			var entityType = ModBlockEntityType.REINFORCED_SHULKER_BOX_MAP.get(material);
			var namespace = BlockEntityType.getId(entityType).getNamespace();
			var name = Text.translatable("container." + namespace + "." + material.getName() + "ShulkerBox");
			var handler = ItemScreenHandler.requireSingleStack((i,p,s)->new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->ReinforcedStorageScreenHandler.createShulkerBoxScreen(material, syncId, inventory, new ShulkerInventory(i, material.getSize(), entityType)), name));

			e.getValue().values().forEach(si->registryFunc.accept((BlockItem)si, handler));
		});

		ClickOpenerMod.LOGGER.info("Reinforced Shulker Compat Loaded");
	}
}
