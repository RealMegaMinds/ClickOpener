package pw.smto.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import pw.smto.clickopener.interfaces.ClosePacketSkipper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("java:S2160")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ClosePacketSkipper {
	protected ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Unique
	@SuppressWarnings("java:S116")
	private boolean clickopener$skipClosePacket;

	@Override
	public void clickopener$setSkipClosePacket(boolean skipClosePacket) {
		clickopener$skipClosePacket = skipClosePacket;
	}

	@Inject(method = "closeHandledScreen", at = @At("HEAD"), cancellable = true)
	private void clickopener$skipClosePacket(CallbackInfo info) {
		if (clickopener$skipClosePacket) {
			onHandledScreenClosed();
			info.cancel();
		}
	}
}
