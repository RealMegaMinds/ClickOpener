package megaminds.clickopener.api;

import megaminds.clickopener.impl.Openable;
import megaminds.clickopener.impl.OpenerHolder;
import megaminds.clickopener.impl.UseAllower;
import megaminds.clickopener.util.ClickContext;
import megaminds.clickopener.util.OpenContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface ItemScreenOpener {
	ItemScreenOpener DEFAULT_OPENER = new ItemScreenOpener() {
	};

	@SuppressWarnings("unused")
	default void preOpen(OpenContext context) {}

	default void open(OpenContext context) {
		if (context.blockState() != null) {
			context.blockState().onUse(context.world(), context.player(), context.hand(), new BlockHitResult(Vec3d.add(context.pos(), .5, 1, .5), Direction.DOWN, context.pos(), false));
		}
	}

	default void postOpen(OpenContext context) {
		final var handler = context.player().currentScreenHandler;
		//Allow inventories to work that don't use the FakeWorld
		if (handler instanceof UseAllower allower) allower.clickopener$allowUse();
		Openable.cast(context.stack()).clickopener$setCloser(()->{
			if (context.player().currentScreenHandler == handler) {
				context.player().closeHandledScreen();
			}
		});
		var holder = ((OpenerHolder)handler);
		holder.clickopener$setOpenStack(context::stack);
		holder.clickopener$addCloseListener(() -> {
			if (context.blockState() != null) {
				//Fake break the block to drop the items
				context.blockState().onStateReplaced(context.world(), context.pos(), Blocks.AIR.getDefaultState(), false);
			}
			context.setStack(getReplacingStack(context));
		});
	}

	default OpenContext constructContext(ClickContext clickContext) {
		var context = new OpenContext(clickContext, this::onStateChange);
		context.setBlock(getBlock(context));
		context.setBlockState(getBlockState(context));
		context.setBlockEntity(getBlockEntity(context));
		return context;
	}

	default void onStateChange(BlockState oldState, OpenContext context) {
		//State is unchanged or is unused -> do nothing
		if (oldState == context.blockState() || oldState == null) return;

		//Change to null/air -> destroy the item and close the screen
		if (context.blockState() == null || context.blockState().isAir()) {
			context.stack().setCount(0);
			return;
		}

		//Assumes other state changes don't close the screen
		context.setStack(getReplacingStack(context));
	}

	default ItemStack getReplacingStack(OpenContext context) {
		if (context.blockState() == null) return ItemStack.EMPTY;
		return context.blockState().getBlock().getPickStack(context.world(), context.pos(), context.blockState());
	}

	default Block getBlock(OpenContext context) {
		if (context.stack().getItem() instanceof BlockItem blockItem) {
			return blockItem.getBlock();
		}
		return null;
	}

	default BlockState getBlockState(OpenContext context) {
		if (context.block() == null) return null;
		var nbt = context.stack().getSubNbt(BlockItem.BLOCK_STATE_TAG_KEY);
		if (nbt != null) {
			nbt.putString("Name", Registries.BLOCK.getId(context.block()).toString());
			var state = NbtHelper.toBlockState(context.world().createCommandRegistryWrapper(RegistryKeys.BLOCK), context.stack().getSubNbt(BlockItem.BLOCK_STATE_TAG_KEY));
			if (!state.isAir()) return state;
		}
		return context.block().getDefaultState();
	}

	default BlockEntity getBlockEntity(OpenContext context) {
		if (context.blockState() != null && context.block() instanceof BlockEntityProvider provider) {
			var blockEntity = provider.createBlockEntity(context.pos(), context.blockState());
			var blockNbt = BlockItem.getBlockEntityNbt(context.stack());
			if (blockNbt != null) {
				blockEntity.readNbt(blockNbt);
			}
			if (context.stack().hasCustomName() && blockEntity instanceof LockableContainerBlockEntity lockable) {
				lockable.setCustomName(context.stack().getName());
			}
			return blockEntity;
		}
		return null;
	}
}
