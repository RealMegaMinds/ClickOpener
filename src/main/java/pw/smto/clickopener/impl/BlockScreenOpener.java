package pw.smto.clickopener.impl;

import pw.smto.clickopener.api.Opener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface BlockScreenOpener extends Opener<BlockScreenOpener, BlockOpenContext> {
	BlockScreenOpener DEFAULT_OPENER = new BlockScreenOpener() {
	};

	@Override
	default BlockOpenContext mutateContext(ClickContext clickContext) {
		if (!(clickContext.initialStack().getItem() instanceof BlockItem)) {
			throw new IllegalArgumentException("BlockItemScreenOpener cannot be used for non-block items.");
		}
		return new BlockOpenContext(clickContext, this);
	}

	@Override
	default ActionResult open(BlockOpenContext context) {
		if (context.initialStack().getCount() != 1) return ActionResult.FAIL;
		var result = context.getBlockState().onUse(context.world(), context.player(), context.hitResult());
		if (result.isAccepted()) {
			return result;
		}
		return context.runWithStackInHand(context::getCursorStack, context::setCursorStack, stack -> stack.useOnBlock(context.toItemUsageContext()));
	}

	@Override
	default void onClose(BlockOpenContext context) {
		//Fake break the block to drop the items
		context.getBlockState().onStateReplaced(context.world(), context.pos(), Blocks.AIR.getDefaultState(), false);
		Opener.super.onClose(context);
	}

	@Override
	default ItemStack getReplacingStack(BlockOpenContext context) {
		return context.getBlockState().getBlock().getPickStack(context.world(), context.pos(), context.getBlockState());
	}

	default void onMarkDirty(BlockOpenContext context) {
		context.setStack(getReplacingStack(context));
	}

	default void onStateChange(BlockState oldState, BlockOpenContext context) {
		//State is unchanged -> do nothing
		if (oldState == context.getBlockState()) return;

		//Change to air -> destroy the item and close the screen
		if (context.getBlockState().isAir()) {
			context.getStack().setCount(0);
			return;
		}

		//Assumes other state changes don't close the screen
		context.setStack(getReplacingStack(context));
	}

	default BlockState getBlockState(BlockOpenContext context) {
		var block = Block.getBlockFromItem(context.getStack().getItem());

		if (context.getStack().getComponents().contains(DataComponentTypes.BLOCK_STATE)) {
			return context.getStack().get(DataComponentTypes.BLOCK_STATE).applyToState(block.getDefaultState());
		}

		return block.getDefaultState();
	}

	default BlockEntity getBlockEntity(BlockOpenContext context) {
		if (!(context.getBlockState().getBlock() instanceof BlockEntityProvider provider)) return null;
		var blockEntity = provider.createBlockEntity(context.pos(), context.getBlockState());
		if (blockEntity != null) blockEntity.readComponents(context.getStack());
		return blockEntity;
	}
}
