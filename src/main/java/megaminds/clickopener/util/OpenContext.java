package megaminds.clickopener.util;

import java.util.Objects;

import megaminds.clickopener.impl.StateChangeListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class OpenContext extends ClickContext {
	private final FakeWorld world;
	private final StateChangeListener listener;
	private Block block;
	private BlockState blockState;
	private BlockEntity blockEntity;

	public OpenContext(ClickContext clickContext, StateChangeListener listener) {
		super(clickContext);
		this.world = FakeWorld.create(this);
		this.listener = listener;
	}

	public FakeWorld world() {
		return world;
	}

	public Block block() {
		return block;
	}

	public BlockState blockState() {
		return blockState;
	}

	public BlockEntity blockEntity() {
		return blockEntity;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public void setBlockState(BlockState state) {
		var oldState = this.blockState;
		this.blockState = state;
		listener.postBlockStateChange(oldState, this);
	}

	public OpenContext setBlockEntity(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		if (blockEntity != null) {
			blockEntity.setWorld(world());
		}
		return this;
	}

	public boolean handles(BlockPos pos) {
		return Objects.equals(pos(), pos);
	}
}

