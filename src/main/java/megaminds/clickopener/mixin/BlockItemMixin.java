package megaminds.clickopener.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;

import eu.pb4.sgui.impl.PlayerExtensions;
import megaminds.clickopener.Config;
import megaminds.clickopener.ShulkerInventory;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
	protected BlockItemMixin(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		if (!player.world.isClient && Config.getClickType()==clickType) {
			var blockState = Block.getBlockFromItem(this).getDefaultState();

			if (blockState.isIn(BlockTags.SHULKER_BOXES)) {
				var cursorStack = player.currentScreenHandler.getCursorStack();
				player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
				
		    	((PlayerExtensions)player).sgui_ignoreNextClose();	//ensures mouse doesn't reset
				if (player.currentScreenHandler==player.playerScreenHandler) {
					((ServerPlayerEntity)player).closeHandledScreen();	//needed for player screen handler
					player.playerScreenHandler.syncState();	//fixes ghost item
				}
				
				player.openHandledScreen(new ShulkerInventory(stack));
				player.currentScreenHandler.setCursorStack(cursorStack);	//keeps the cursor stack in the cursor after switching screens
				return true;
			}
		}

		return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
	}
}

// !!Fletching tables don't have screens!!

////@Mixin(Item.class)
////public abstract class BlockItemMixin {	
//	private static final NamedScreenHandlerFactory ENDER_CHEST_HANDLER_FACTORY = new SimpleNamedScreenHandlerFactory((syncId, inventory, player)->GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player.getEnderChestInventory()), new TranslatableText("container.enderchest"));
//
////	@SuppressWarnings("deprecation")
////	@Inject(method = "onClicked", at = @At(value = "HEAD"), cancellable = true)
////	private void checkBlockClicks(ItemStack myStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> info) {
////		//If on the server, the stack is of a BlockItem, and was right clicked...
////		if (!player.world.isClient && ((Object)this) instanceof BlockItem blockItem && clickType==ClickType.RIGHT) {	//NOSONAR
////			var block = blockItem.getBlock();
////			if (block == Blocks.FLETCHING_TABLE) return;	//this extends CraftingBlock but doesn't actually have a screen
//
////			var blockRegEntry = block.getRegistryEntry();
////			NamedScreenHandlerFactory fact = null;
//			
//			if (block instanceof BlockWithEntity bwe) {
//				//these ones might need to have their own individual methods
//				//blastfurnace, smoker, dropper, dispenser, chest, hopper, trapped chest, barrel, furnace, enchanttable, shulkerbox
//
//				if (blockRegEntry.isIn(BlockTags.SHULKER_BOXES)) {
//					//***************Trying a different method; create state and entity from item and then create screenhandler from entity*****************
////					var blockState = block.getDefaultState();
////					var stateNBT = myStack.getSubNbt(BlockItem.BLOCK_STATE_TAG_KEY);
////					if (stateNBT != null) {
////						StateManager<Block, BlockState> stateManager = blockState.getBlock().getStateManager();
////						for (String string : stateNBT.getKeys()) {
////							Property<?> property = stateManager.getProperty(string);
////							if (property == null) continue;
////							String string2 = stateNBT.get(string).asString();
////							property.parse(string2).map(value -> (BlockState)blockState.with(property, value)).orElse(blockState);
////						}
////					}
//					
////			        BlockItem.writeNbtToBlockEntity(world, player, pos, stack)        BlockEntity blockEntity;
////			        MinecraftServer minecraftServer = world.getServer();
////			        if (minecraftServer == null) {
////			            return false;
////			        }
////			        NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(stack);
////			        if (nbtCompound != null && (blockEntity = world.getBlockEntity(pos)) != null) {
////			            if (!(world.isClient || !blockEntity.copyItemDataRequiresOperator() || player != null && player.isCreativeLevelTwoOp())) {
////			                return false;
////			            }
////			            NbtCompound nbtCompound2 = blockEntity.createNbt();
////			            NbtCompound nbtCompound3 = nbtCompound2.copy();
////			            nbtCompound2.copyFrom(nbtCompound);
////			            if (!nbtCompound2.equals(nbtCompound3)) {
////			                blockEntity.readNbt(nbtCompound2);
////			                blockEntity.markDirty();
////			                return true;
////			            }
////			        }
////			        return false;
//			        //****************************************
//			        
//					fact = (NamedScreenHandlerFactory) bwe.createBlockEntity(player.getBlockPos(), bwe.getDefaultState());
//				}
//
//				//must have its own
//				if (blockItem == Items.ENDER_CHEST) {
//					fact = ENDER_CHEST_HANDLER_FACTORY;
//				}
//			} else {
//				//this works for SmithingTable, CraftingTable, Grindstone, StoneCutter, CartographyTable, Loom
//				//anvils work except that custom names are removed (probably should see if I can fix that)
//				fact = block.createScreenHandlerFactory(null, player.getWorld(), player.getBlockPos());
//			}
//			
//			if (fact != null) {
//				player.openHandledScreen(new PermissiveNamedScreenHandlerFactory(fact));
//				info.setReturnValue(true);
//			}
//
//// else if (blockRegEntry.isIn(BlockTags.SHULKER_BOXES)) {
////				player.openHandledScreen(new ShulkerInventory(myStack));
////			} else if (blockItem == Items.ENCHANTING_TABLE) {	//can use nearby bookshelves
////				player.openHandledScreen(new PermissiveNamedScreenHandlerFactory((syncId, inventory, player2) -> new EnchantmentScreenHandler(syncId, inventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos())), blockItem.getName()));
////			} else if (blockRegEntry.isIn(BlockTags.ANVIL)) {
////				player.openHandledScreen(CRAFTING_TABLE_HANDLER_FACTORY);
////			} else {
////				return;
////			}
//		}
//	}
//}