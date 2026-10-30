package draylar.tiered.reforge;

import draylar.tiered.Tiered;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.api.TierComponent;
import draylar.tiered.api.TieredItemTags;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.network.TieredServerPacket;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.LevelEvent;

public class ReforgeScreenHandler extends AbstractContainerMenu {

    private final Container inventory = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            ReforgeScreenHandler.this.slotsChanged(this);
        }
    };

    private final ContainerLevelAccess context;
    private final Player player;
    private boolean reforgeReady;
    private BlockPos pos;

    public ReforgeScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(Tiered.REFORGE_SCREEN_HANDLER_TYPE, syncId);

        this.context = context;
        this.player = playerInventory.player;
        this.addSlot(new Slot(this.inventory, 0, 45, 47));
        this.addSlot(new Slot(this.inventory, 1, 80, 34));
        this.addSlot(new Slot(this.inventory, 2, 115, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TieredItemTags.REFORGE_ADDITION);
            }
        });

        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
        this.context.execute((world, pos) -> {
            ReforgeScreenHandler.this.setPos(pos);
        });
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        if (!player.level().isClientSide() && inventory == this.inventory) {
            this.updateResult();
        }

    }

    private void updateResult() {
        this.reforgeReady = this.isReforgeReady();
        TieredServerPacket.writeS2CReforgeReadyPacket((ServerPlayer) player, !this.reforgeReady);
    }

    private boolean isReforgeReady() {
        ItemStack stack = this.getSlot(1).getItem();
        boolean ready = false;
        if (this.getSlot(0).hasItem() && this.getSlot(1).hasItem() && this.getSlot(2).hasItem() && this.getSlot(2).getItem().is(TieredItemTags.REFORGE_ADDITION)) {
            Item item = stack.getItem();
            if (!stack.is(TieredItemTags.MODIFIER_RESTRICTED) && ModifierUtils.getRandomAttributeIDFor(null, item, false) != null && !stack.isDamaged()) {
                List<Item> items = Tiered.REFORGE_DATA_LOADER.getReforgeBaseItems(item);
                ItemStack baseItem = this.getSlot(0).getItem();
                if (!items.isEmpty()) {
                    ready = items.stream().anyMatch(it -> it == baseItem.getItem());
                } else if (item instanceof TieredItem toolItem) {
                    ready = toolItem.getTier().getRepairIngredient().test(baseItem);
                } else if (item instanceof ArmorItem armorItem && armorItem.getMaterial().value().repairIngredient() != null) {
                    ready = armorItem.getMaterial().value().repairIngredient().get().test(baseItem);
                } else {
                    ready = baseItem.is(TieredItemTags.REFORGE_BASE_ITEM);
                }
            }
        }
        if (ready && !ConfigInit.CONFIG.uniqueReforge && ModifierUtils.getAttributeId(stack) != null && ModifierUtils.getAttributeId(stack).getPath().contains("unique")) {
            ready = false;
        }
        return ready;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.context.execute((world, pos) -> this.clearContainer(player, this.inventory));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.context.evaluate((world, pos) -> {
            if (!world.getBlockState(pos).is(BlockTags.ANVIL)) {
                return false;
            }
            return player.distanceToSqr((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64.0;
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index == 1) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemStack2, itemStack);
            } else if (index == 0 || index == 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 3 && index < 39) {
                if (itemStack.is(TieredItemTags.REFORGE_ADDITION) && !this.moveItemStackTo(itemStack2, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }
                if (this.getSlot(1).hasItem()) {
                    Item item = this.getSlot(1).getItem().getItem();
                    if (item instanceof TieredItem toolItem && toolItem.getTier().getRepairIngredient().test(itemStack) && !this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    if (item instanceof ArmorItem armorItem && armorItem.getMaterial().value().repairIngredient() != null && armorItem.getMaterial().value().repairIngredient().get().test(itemStack)
                            && !this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    if (itemStack.is(TieredItemTags.REFORGE_BASE_ITEM) && !this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    List<Item> items = Tiered.REFORGE_DATA_LOADER.getReforgeBaseItems(item);
                    if (items.stream().anyMatch(it -> it == itemStack2.copy().getItem()) && !this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (ModifierUtils.getRandomAttributeIDFor(null, itemStack.getItem(), false) != null && !this.moveItemStackTo(itemStack2, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack2);
        }
        return itemStack;
    }

    public void reforge() {
        if (!this.isReforgeReady()) {
            return;
        }
        ItemStack itemStack = this.getSlot(1).getItem();
        TierComponent oldTier = itemStack.get(Tiered.TIER);
        ModifierUtils.removeItemStackAttribute(itemStack);
        ModifierUtils.setItemStackAttribute(player, itemStack, true);
        if (itemStack.get(Tiered.TIER) == null) {
            if (oldTier != null) {
                itemStack.set(Tiered.TIER, oldTier);
            }
            return;
        }

        this.decrementStack(0);
        this.decrementStack(2);
        this.context.execute((world, pos) -> world.levelEvent(LevelEvent.SOUND_ANVIL_USED, (BlockPos) pos, 0));
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.inventory.getItem(slot);
        itemStack.shrink(1);
        this.inventory.setItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.inventory && super.canTakeItemForPickAll(stack, slot);
    }

}
