package megaminds.clickopener.api;

import megaminds.clickopener.impl.ClickContext;
import megaminds.clickopener.interfaces.OpenContextHolder;
import megaminds.clickopener.interfaces.Openable;
import megaminds.clickopener.interfaces.UseAllower;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

@SuppressWarnings("unused")
public interface Opener<SELF extends Opener<SELF, T>, T extends OpenContext<T, SELF>> {
	T mutateContext(ClickContext context);

	default void preOpen(T context) {}

	ActionResult open(T context);

	default void postOpen(T context) {
		final var handler = context.player().currentScreenHandler;
		//Allow any ScreenHandlers that need to be forced
		if (handler instanceof UseAllower allower) allower.clickopener$allowUse();
		Openable.cast(context.getStack()).clickopener$setCloser(()->{
			if (context.player().currentScreenHandler == handler) {
				context.player().closeHandledScreen();
			}
		});
		((OpenContextHolder)handler).clickopener$setOpenContext(context);
	}

	default void onClose(T context) {
		Openable.cast(context.getStack()).clickopener$clearCloser();
	}

	default ItemStack getReplacingStack(T context) {
		return context.getStack();
	}
}
