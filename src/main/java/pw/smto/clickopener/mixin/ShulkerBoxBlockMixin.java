package pw.smto.clickopener.mixin;

import java.util.Optional;

import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(ShulkerBoxBlock.class)
public abstract class ShulkerBoxBlockMixin {
	@SuppressWarnings("unused")
	@Redirect(method = "getPickStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntityType;)Ljava/util/Optional;"))
	public Optional<? extends BlockEntity> replaceGetBlockEntity(WorldView world, BlockPos pos, BlockEntityType<?> type) {
		return Optional.ofNullable(world.getBlockEntity(pos)).filter(ShulkerBoxBlockEntity.class::isInstance);
	}
}
