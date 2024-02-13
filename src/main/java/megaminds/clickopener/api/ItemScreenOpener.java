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
import net.minecraft.nbt.NbtElement;
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
				context.blockState().onStateReplaced(context.world(), context.pos(), Blocks.AIR.getDefaultState(), false);
			}
			if (context.blockEntity() != null) {
				context.blockEntity().setStackNbt(context.stack());
			}
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
		//If the state didn't change or is unused, do nothing
		if (oldState == context.blockState() || oldState == null) return;
		if (context.blockState() == null || context.blockState().isAir()) {
			context.stack().setCount(0);
			return;
		}
		context.setStack(getReplacingStack(context));
	}

	default ItemStack getReplacingStack(OpenContext context) {
		var stack = context.blockState().getBlock().asItem().getDefaultStack();
		if (context.blockState() != context.blockState().getBlock().getDefaultState()) {
			var nbt = NbtHelper.fromBlockState(context.blockState());
			nbt.remove("Name");
			stack.setSubNbt(BlockItem.BLOCK_STATE_TAG_KEY, nbt);
		}
		if (context.blockEntity() != null) {
			var nbt = context.blockEntity().createNbt();
			if (nbt.getList("Items", NbtElement.COMPOUND_TYPE).isEmpty()) {
				nbt.remove("Items");
			}
			if (nbt.isEmpty()) {
				stack.removeSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
			} else {
				BlockEntity.writeIdToNbt(nbt, context.blockEntity().getType());
				stack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, nbt);
			}
		}
		return stack;
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
