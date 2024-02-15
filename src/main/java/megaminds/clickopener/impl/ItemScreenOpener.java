package megaminds.clickopener.impl;

import megaminds.clickopener.api.Opener;
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
		var handStack = context.player().getStackInHand(context.hand());
		context.player().setStackInHand(context.hand(), context.getStack());
		var result = context.getStack().use(context.player().getServerWorld(), context.player(), context.hand()).getResult();
		context.setStack(context.player().getStackInHand(context.hand()));
		context.player().setStackInHand(context.hand(), handStack);
		return result;
	}
}
