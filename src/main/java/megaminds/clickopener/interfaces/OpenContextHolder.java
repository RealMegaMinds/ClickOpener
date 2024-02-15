package megaminds.clickopener.interfaces;

import megaminds.clickopener.api.OpenContext;

public interface OpenContextHolder {
	void clickopener$setOpenContext(OpenContext<?, ?> openContext);
	boolean clickopener$hasOpenContext();
}
