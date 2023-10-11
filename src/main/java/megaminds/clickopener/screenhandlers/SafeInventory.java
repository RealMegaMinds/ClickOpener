//package megaminds.clickopener.screenhandlers;
//
//import java.util.UUID;
//
//import megaminds.clickopener.api.BlockEntityInventory;
//import megaminds.clickopener.compat.SupplementariesCompat;
//import net.minecraft.block.entity.BlockEntityType;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.registry.RegistryKeys;
//import net.minecraft.registry.tag.TagKey;
//import net.minecraft.util.Hand;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.collection.DefaultedList;
//
//public class SafeInventory extends BlockEntityInventory {
//	private String password;
//	private String ownerName;
//	private UUID owner;
//
//	public SafeInventory(ItemStack link, int size, BlockEntityType<?> entityType, DefaultedList<ItemStack> inventory, boolean acceptDelegateSlots) {
//		super(link, size, entityType, inventory, acceptDelegateSlots);
//	}
//
//	public SafeInventory(ItemStack link, int size, BlockEntityType<?> entityType, DefaultedList<ItemStack> inventory) {
//		super(link, size, entityType, inventory);
//	}
//
//	public SafeInventory(ItemStack link, int size, BlockEntityType<?> entityType, boolean acceptDelegateSlots) {
//		super(link, size, entityType, acceptDelegateSlots);
//	}
//
//	public SafeInventory(ItemStack link, int size, BlockEntityType<?> entityType) {
//		super(link, size, entityType);
//	}
//	
//	private void handleAction(PlayerEntity player, Hand hand) {
//		var stack = player.getStackInHand(hand);
//
//        //clear ownership with tripwire
//        var cleared = false;
//        if ((boolean) SupplementariesCompat.getConfigOption("SAFE_SIMPLE")) {
//            if ((stack.isOf(Items.TRIPWIRE_HOOK) || stack.isIn(TagKey.of(RegistryKeys.ITEM, new Identifier("supplementaries", "key")))) &&
//                    (this.isOwnedBy(player) || (this.isNotOwnedBy(player) && player.isCreative()))) {
//                cleared = true;
//            }
//        } else {
//            if (player.isShiftKeyDown() && (player.isCreative() || this.getKeyStatus(stack).isCorrect())) {
//                cleared = true;
//            }
//        }
//
//        if (cleared) {
//            this.clearPassword();
//            this.onPasswordCleared(player, worldPosition);
//            return true;
//        }
//
//        BlockPos frontPos = worldPosition.relative(getBlockState().getValue(SafeBlock.FACING));
//        if (!level.getBlockState(frontPos).isRedstoneConductor(level, frontPos)) {
//            if (CommonConfigs.Functional.SAFE_SIMPLE.get()) {
//                UUID owner = this.getOwner();
//                if (owner == null) {
//                    owner = player.getUUID();
//                    this.setOwner(owner);
//                }
//                if (!owner.equals(player.getUUID())) {
//                    player.displayClientMessage(Component.translatable("message.supplementaries.safe.owner", this.ownerName), true);
//                    if (!player.isCreative()) {
//                        return true;
//                    }
//                }
//            } else {
//                String key = this.getPassword();
//                if (key == null) {
//                    String newKey = IKeyLockable.getKeyPassword(stack);
//                    if (newKey != null) {
//                        this.setPassword(newKey);
//                        this.onKeyAssigned(level, worldPosition, player, newKey);
//                        return true;
//                    }
//                } else if (!this.canPlayerOpen(player, true) && !player.isCreative()) {
//                    return true;
//                }
//            }
//            PlatHelper.openCustomMenu((ServerPlayer) player, this, worldPosition);
//
//            PiglinAi.angerNearbyPiglins(player, true);
//        }
//
//        return true;
//	}
//}
