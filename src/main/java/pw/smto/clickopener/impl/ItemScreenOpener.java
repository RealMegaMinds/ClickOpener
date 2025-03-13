package pw.smto.clickopener.impl;

import pw.smto.clickopener.api.Opener;
import net.minecraft.util.ActionResult;

public interface ItemScreenOpener extends Opener<ItemScreenOpener, ItemOpenContext> {
	ItemScreenOpener DEFAULT_OPENER = new ItemScreenOpener() {
	};

	@Override
	default ItemOpenContext mutateContext(ClickContext context) {
		return new ItemOpenContext(context, this);
	}

	@Override
	default ActionResult open(ItemOpenContext context) {
		return context.runWithStackInHand(context::getStack, context::setStack, stack -> {
			var result = stack.use(context.player().getServerWorld(), context.player(), context.hand());
			context.player().setStackInHand(context.hand(), result.getValue());
			return result.getResult();
		});
	}
}
