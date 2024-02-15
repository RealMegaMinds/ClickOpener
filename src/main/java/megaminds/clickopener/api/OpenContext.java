package megaminds.clickopener.api;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import megaminds.clickopener.impl.ClickContext;
import megaminds.clickopener.interfaces.Openable;
import net.minecraft.item.ItemStack;

public abstract class OpenContext<SELF extends OpenContext<SELF, O>, O extends Opener<O, SELF>> extends ClickContext {
	private final O opener;
	private ItemStack cursorStack;
	private ItemStack stack;

	protected OpenContext(ClickContext context, O opener) {
		super(context);
		this.opener = opener;
		this.stack = initialStack();
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

	public void setStack(ItemStack stack) {
		clickedInventory().setStack(slotIndex(), ItemStack.EMPTY);	//Set stack to empty while reassigning closer to try avoiding concurrency issues
		Openable.cast(stack).clickopener$setCloser(Openable.cast(getStack()).clickopener$clearCloser());
		this.stack = stack;
		clickedInventory().setStack(slotIndex(), stack);
	}

	public void setCursorStack(ItemStack cursorStack) {
		this.cursorStack = cursorStack;
	}

	public void openerConsumer(BiConsumer<O, SELF> consumer) {
		consumer.accept(opener(), self());
	}

	public <R> R openerFunction(BiFunction<O, SELF, R> consumer) {
		return consumer.apply(opener(), self());
	}
}
