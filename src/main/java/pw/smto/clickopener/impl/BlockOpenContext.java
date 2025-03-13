package pw.smto.clickopener.impl;

import java.util.Objects;

import pw.smto.clickopener.api.OpenContext;
import pw.smto.clickopener.util.FakeWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class BlockOpenContext extends OpenContext<BlockOpenContext, BlockScreenOpener> {
	private final FakeWorld world;
	private BlockState blockState;
	private BlockEntity blockEntity;

	public BlockOpenContext(ClickContext context, BlockScreenOpener opener) {
		super(context, opener);
		this.world = FakeWorld.create(this);
		this.blockState = opener.getBlockState(this);
		this.blockEntity = opener.getBlockEntity(this);
		if (blockEntity != null) blockEntity.setWorld(world());
	}

	public BlockOpenContext(BlockOpenContext context, BlockScreenOpener opener) {
		this((ClickContext) context, opener);
		this.blockState = context.getBlockState();
		this.blockEntity = context.getBlockEntity();
	}

	@Override
	public FakeWorld world() {
		return world;
	}

	public BlockState getBlockState() {
		return blockState;
	}

	public BlockEntity getBlockEntity() {
		return blockEntity;
	}

	public void setBlockState(BlockState state) {
		var oldState = this.blockState;
		this.blockState = state;
		opener().onStateChange(oldState, this);
	}

	public BlockOpenContext setBlockEntity(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		if (blockEntity != null) {
			blockEntity.setWorld(world());
		}
		return this;
	}

	public boolean handles(BlockPos pos) {
		return Objects.equals(pos(), pos);
	}

	@Override
	public BlockOpenContext self() {
		return this;
	}
}

