package pw.smto.clickopener.util;

@SuppressWarnings("serial")
public class ItemOpenException extends RuntimeException {
	public ItemOpenException() {}
	public ItemOpenException(String message) {
		super(message);
	}
	public ItemOpenException(Throwable cause) {
		super(cause);
	}
	public ItemOpenException(String message, Throwable cause) {
		super(message, cause);
	}
}
