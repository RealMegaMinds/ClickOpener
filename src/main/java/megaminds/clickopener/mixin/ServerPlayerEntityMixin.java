package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import megaminds.clickopener.impl.CloseIgnorer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements CloseIgnorer {
	@Shadow public abstract void closeScreenHandler();

	@Unique
	private boolean clickOpener_ignoreNext = false;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, PlayerPublicKey publicKey) {
		super(world, pos, yaw, gameProfile, publicKey);
	}

	@Inject(method = "closeHandledScreen", at = @At("HEAD"), cancellable = true)
	private void clickOpener_ignoreClosing(CallbackInfo ci) {
		if (this.clickOpener_ignoreNext) {
			this.clickOpener_ignoreNext = false;
			this.closeScreenHandler();
			ci.cancel();
		}
	}

	@Override
	public void clickOpener_ignoreNextClose() {
		this.clickOpener_ignoreNext = true;
	}
}
