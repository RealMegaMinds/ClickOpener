package megaminds.clickopener.screenhandlers;

import java.util.List;
import java.util.OptionalInt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

/**
 * {@link #canUse(PlayerEntity)} always returns true. All other methods are delegated to {@link #delegate}.
 */
public class PermissiveScreenHandler extends ScreenHandler {
	private final ScreenHandler delegate;

	public PermissiveScreenHandler(ScreenHandler delegate) {
		super(delegate.getType(), delegate.syncId);
		this.delegate = delegate;
	}

	@Override
	public boolean canUse(PlayerEntity var1) {
		return true;
	}

	@Override
	public ScreenHandlerType<?> getType() {
		return delegate.getType();
	}

	@Override
	public boolean isValid(int slot) {
		return delegate.isValid(slot);
	}

	@Override
	public void addListener(ScreenHandlerListener listener) {
		delegate.addListener(listener);
	}

	@Override
	public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
		delegate.updateSyncHandler(handler);
	}

	@Override
	public void syncState() {
		delegate.syncState();
	}

	@Override
	public void removeListener(ScreenHandlerListener listener) {
		delegate.removeListener(listener);
	}

	@Override
	public DefaultedList<ItemStack> getStacks() {
		return delegate.getStacks();
	}

	@Override
	public void sendContentUpdates() {
		delegate.sendContentUpdates();
	}

	@Override
	public void updateToClient() {
		delegate.updateToClient();
	}

	@Override
	public void setPreviousTrackedSlot(int slot, ItemStack stack) {
		delegate.setPreviousTrackedSlot(slot, stack);
	}

	@Override
	public void setPreviousTrackedSlotMutable(int slot, ItemStack stack) {
		delegate.setPreviousTrackedSlotMutable(slot, stack);
	}

	@Override
	public void setPreviousCursorStack(ItemStack stack) {
		delegate.setPreviousCursorStack(stack);
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		return delegate.onButtonClick(player, id);
	}

	@Override
	public Slot getSlot(int index) {
		return delegate.getSlot(index);
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		return delegate.transferSlot(player, index);
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		delegate.onSlotClick(slotIndex, button, actionType, player);
	}

	@Override
	public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
		return delegate.canInsertIntoSlot(stack, slot);
	}

	@Override
	public void close(PlayerEntity player) {
		delegate.close(player);
	}

	@Override
	public void onContentChanged(Inventory inventory) {
		delegate.onContentChanged(inventory);
	}

	@Override
	public void setStackInSlot(int slot, int revision, ItemStack stack) {
		delegate.setStackInSlot(slot, revision, stack);
	}

	@Override
	public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
		delegate.updateSlotStacks(revision, stacks, cursorStack);
	}

	@Override
	public void setProperty(int id, int value) {
		delegate.setProperty(id, value);
	}

	@Override
	public boolean canInsertIntoSlot(Slot slot) {
		return delegate.canInsertIntoSlot(slot);
	}

	@Override
	public void setCursorStack(ItemStack stack) {
		delegate.setCursorStack(stack);
	}

	@Override
	public ItemStack getCursorStack() {
		return delegate.getCursorStack();
	}

	@Override
	public void disableSyncing() {
		delegate.disableSyncing();
	}

	@Override
	public void enableSyncing() {
		delegate.enableSyncing();
	}

	@Override
	public void copySharedSlots(ScreenHandler handler) {
		delegate.copySharedSlots(handler);
	}

	@Override
	public OptionalInt getSlotIndex(Inventory inventory, int index) {
		return delegate.getSlotIndex(inventory, index);
	}

	@Override
	public int getRevision() {
		return delegate.getRevision();
	}

	@Override
	public int nextRevision() {
		return delegate.nextRevision();
	}
}