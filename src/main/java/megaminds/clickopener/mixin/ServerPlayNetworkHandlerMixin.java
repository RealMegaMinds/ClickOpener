package megaminds.clickopener.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import megaminds.clickopener.CloseIgnorer;
import megaminds.clickopener.GuiScheduler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements GuiScheduler {
	@Shadow
	private ServerPlayerEntity player;

	@Unique
	private NamedScreenHandlerFactory fac;

	@Inject(at = @At(value = "TAIL"), method = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onClickSlot(Lnet/minecraft/network/packet/c2s/play/ClickSlotC2SPacket;)V")
	public void clickopenerEndClick(ClickSlotC2SPacket packet, CallbackInfo info) {
		if (fac != null) {
			var cursorStack = player.currentScreenHandler.getCursorStack();
			player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);

			((CloseIgnorer)player).clickOpener_ignoreNextClose();	//ensures mouse doesn't reset
			if (player.currentScreenHandler==player.playerScreenHandler) {
				//Need to manually close the player's inventory screen
				player.closeHandledScreen();
			}

			player.openHandledScreen(fac);
			player.currentScreenHandler.setCursorStack(cursorStack);	//keeps the cursor stack in the cursor after switching screens
			fac = null;
		}
	}

	@Unique
	@Override
	public void scheduleOpenGui(NamedScreenHandlerFactory factory) {
		fac = factory;
	}
}