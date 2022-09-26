package megaminds.clickopener.impl;

public interface Openable {
	void clickopener_open(Runnable closer);
	boolean clickopener_isOpen();
	void clickopener_close();
}
