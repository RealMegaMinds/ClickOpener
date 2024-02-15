package megaminds.clickopener.impl;

import megaminds.clickopener.api.OpenContext;

public class ItemOpenContext extends OpenContext<ItemOpenContext, ItemScreenOpener> {
	public ItemOpenContext(ClickContext context, ItemScreenOpener opener) {
		super(context, opener);
	}

	@Override
	public ItemOpenContext self() {
		return this;
	}
}
