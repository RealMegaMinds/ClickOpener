package megaminds.clickopener.impl;

public interface Openable {
	void clickopener$setCloser(Runnable closer);
	boolean clickopener$hasCloser();
	default void clickopener$clearCloser() {
		clickopener$setCloser(null);
	}
}
