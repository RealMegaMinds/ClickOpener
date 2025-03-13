package pw.smto.clickopener.interfaces;

import pw.smto.clickopener.api.OpenContext;

public interface OpenContextHolder {
	void clickopener$setOpenContext(OpenContext<?, ?> openContext);
	boolean clickopener$hasOpenContext();
}
