package megaminds.clickopener.screenhandlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class WrappedFactory implements NamedScreenHandlerFactory {
	private final NamedScreenHandlerFactory delegate;

	private WrappedFactory(NamedScreenHandlerFactory delegate) {
		this.delegate = delegate;
	}

	@Override
	public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return delegate.createMenu(i, playerInventory, playerEntity);
	}

	@Override
	public Text getDisplayName() {
		return delegate.getDisplayName();
	}

	@Override
	public boolean shouldCloseCurrentScreen() {
		return false;
	}

	public static WrappedFactory wrap(NamedScreenHandlerFactory delegate) {
		return delegate instanceof WrappedFactory wrapped ? wrapped : new WrappedFactory(delegate);
	}
}
