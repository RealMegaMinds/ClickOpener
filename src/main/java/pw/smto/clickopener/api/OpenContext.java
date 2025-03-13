package pw.smto.clickopener.api;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pw.smto.clickopener.impl.ClickContext;
import pw.smto.clickopener.interfaces.Openable;
import net.minecraft.item.ItemStack;

public abstract class OpenContext<SELF extends OpenContext<SELF, O>, O extends Opener<O, SELF>> extends ClickContext {
	private final O opener;
	private ItemStack cursorStack;
	private ItemStack stack;
	private boolean syncing;

	protected OpenContext(ClickContext context, O opener) {
		super(context);
		this.opener = opener;
		this.cursorStack = initialCursorStack();
		this.stack = initialStack();
		this.syncing = true;
	}

	public abstract SELF self();

	public O opener() {
		return opener;
	}

	public ItemStack getStack() {
		return stack;
	}

	public ItemStack getCursorStack() {
		return cursorStack;
	}
	
	public boolean isSyncing() {
		return syncing;
	}
	
	public void setSyncing(boolean syncing) {
		this.syncing = syncing;
		sync();
	}
	
	public void sync() {
		if (!isSyncing()) return;
		clickedInventory().setStack(slotIndex(), getStack());
	}

	public void setStack(ItemStack stack) {
		Openable.cast(stack).clickopener$setCloser(Openable.cast(getStack()).clickopener$clearCloser());
		this.stack = stack;
		sync();
	}

	public void setCursorStack(ItemStack cursorStack) {
		this.cursorStack = cursorStack;
	}

	public <T> T runWithStackInHand(Supplier<ItemStack> stackSupplier, Consumer<ItemStack> stackReplacer, Function<ItemStack, T> action) {
		var actionStack = stackSupplier.get();
		var originalHandStack = player().getStackInHand(hand());
		if (actionStack == originalHandStack) return action.apply(originalHandStack);

		setSyncing(false);
		player().setStackInHand(hand(), actionStack);
		var result = action.apply(actionStack);
		stackReplacer.accept(player().getStackInHand(hand()));
		player().setStackInHand(hand(), originalHandStack);
		setSyncing(true);
		return result;
	}

	public void openerConsumer(BiConsumer<O, SELF> consumer) {
		consumer.accept(opener(), self());
	}

	public <R> R openerFunction(BiFunction<O, SELF, R> consumer) {
		return consumer.apply(opener(), self());
	}
}
