package megaminds.clickopener.api;

import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;

public class HandlerRegisterEvent {
	public static final Identifier VANILLA_PHASE = new Identifier("vanilla");

	public static final Event<HandlerRegisterEventListener> EVENT = EventFactory.createWithPhases(HandlerRegisterEventListener.class, listeners -> registyFunc -> {
		for (var listener : listeners) {
			listener.onRegister(registyFunc);
		}
	}, VANILLA_PHASE, Event.DEFAULT_PHASE);

	public interface HandlerRegisterEventListener {
		void onRegister(BiConsumer<BlockItem, ItemScreenOpener> registyFunc);
	}
}
