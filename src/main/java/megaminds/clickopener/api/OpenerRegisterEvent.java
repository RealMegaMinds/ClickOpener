package megaminds.clickopener.api;

import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.BlockItem;

public class OpenerRegisterEvent {
	public static final Event<OpenerRegisterEventListener> EVENT = EventFactory.createArrayBacked(OpenerRegisterEventListener.class, listeners -> registyFunc -> {
		for (var listener : listeners) {
			listener.onRegister(registyFunc);
		}
	});

	public interface OpenerRegisterEventListener {
		void onRegister(BiConsumer<BlockItem, ItemScreenOpener> registyFunc);
	}
}
