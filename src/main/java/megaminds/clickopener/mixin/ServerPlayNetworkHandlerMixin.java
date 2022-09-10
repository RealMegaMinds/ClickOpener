package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import megaminds.clickopener.api.ClickType;
import megaminds.clickopener.util.ScreenHelper;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	private ServerPlayerEntity player;

	@Inject(at = @At(value = "INVOKE", target = "net/minecraft/network/packet/c2s/play/ClickSlotC2SPacket.getRevision()I", shift = Shift.BEFORE), method = "onClickSlot", cancellable = true)
	public void clickopener_onClickSlot(ClickSlotC2SPacket packet, CallbackInfo info) {
		var s = packet.getSlot();
		var clickType = ClickType.convert(packet.getActionType(), packet.getButton(), s);
		if (s==ScreenHandler.EMPTY_SPACE_SLOT_INDEX || s==-1 || ClickType.OTHER.equals(clickType)) return;

		var slot = player.currentScreenHandler.getSlot(s);
		if (ScreenHelper.openScreen(player, clickType, slot.getStack(), slot.inventory)) info.cancel();
	}
}