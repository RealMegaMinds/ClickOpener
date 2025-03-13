package pw.smto.clickopener.impl;

import pw.smto.clickopener.api.OpenContext;

public class ItemOpenContext extends OpenContext<ItemOpenContext, ItemScreenOpener> {
	public ItemOpenContext(ClickContext context, ItemScreenOpener opener) {
		super(context, opener);
	}

	@Override
	public ItemOpenContext self() {
		return this;
	}
}
