package pw.smto.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pw.smto.clickopener.api.ClickType;
import pw.smto.clickopener.impl.ClickContext;
import pw.smto.clickopener.interfaces.Openable;
import pw.smto.clickopener.util.ScreenHelper;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	private ServerPlayerEntity player;

	@Inject(at = @At(value = "INVOKE", target = "net/minecraft/network/packet/c2s/play/ClickSlotC2SPacket.getRevision()I", shift = Shift.BEFORE), method = "onClickSlot", cancellable = true)
	public void clickopener_onClickSlot(ClickSlotC2SPacket packet, CallbackInfo info) {
		var slotIndex = packet.getSlot();
		if (slotIndex==ScreenHandler.EMPTY_SPACE_SLOT_INDEX || slotIndex==-1) {
			//use Minecraft default handling
			return;
		}

		var slot = player.currentScreenHandler.getSlot(slotIndex);
		var stack = slot.getStack();
		if (stack!=null && ((Openable)(Object)stack).clickopener$hasCloser()) {
			//Do nothing/revert picking up the item
			player.currentScreenHandler.syncState();
			info.cancel();
			return;
		}

		var clickType = ClickType.convert(packet.getActionType(), packet.getButton(), slotIndex);
		if (ClickType.NONE.equals(clickType)) {
			//use Minecraft default handling
			return;
		}

		for (var hand : Hand.values()) {
			if (ScreenHelper.openScreen(new ClickContext(player, hand, slot.inventory, slot.getIndex(), clickType, player.currentScreenHandler.getCursorStack(), stack))) {
				//Successfully opened, so don't do anything else
				info.cancel();
				return;
			}//else Minecraft default handling
		}
	}	
}