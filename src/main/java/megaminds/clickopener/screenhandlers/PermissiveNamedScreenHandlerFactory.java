package megaminds.clickopener.screenhandlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;

/**
 * {@link #createMenu(int, PlayerInventory, PlayerEntity)} returns {@link PermissiveScreenHandler}s wrapped around what would normally be returned.
 */
public record PermissiveNamedScreenHandlerFactory(NamedScreenHandlerFactory delegate) implements NamedScreenHandlerFactory {
	public PermissiveNamedScreenHandlerFactory(ScreenHandlerFactory delegate, Text displayName) {
		this(new SimpleNamedScreenHandlerFactory(delegate, displayName));
	}

	@Override
	public Text getDisplayName() {
		return delegate.getDisplayName();
	}

	/**
	 * Wraps the {@link ScreenHandler} created by the base factory in a {@link PermissiveScreenHandler}.
	 */
	@Override
	public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return new PermissiveScreenHandler(this.delegate.createMenu(i, playerInventory, playerEntity));
	}
}