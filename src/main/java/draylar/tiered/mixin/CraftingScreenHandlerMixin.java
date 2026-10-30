package draylar.tiered.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingMenu.class)
public class CraftingScreenHandlerMixin {

    @Inject(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"), cancellable = true)
    public void quickMoveMixin(Player player, int slot, CallbackInfoReturnable<ItemStack> info) {
        AbstractContainerMenu screenHandler = (AbstractContainerMenu) (Object) this;
        ItemStack result = screenHandler.getSlot(slot).getItem();
        for (int i = 10; i < 46; i++) {
            Slot targetSlot = screenHandler.getSlot(i);
            ItemStack targetStack = targetSlot.getItem();
            if (targetStack.isEmpty() ? targetSlot.mayPlace(result)
                    : ItemStack.isSameItemSameComponents(targetStack, result) && targetStack.getCount() < targetSlot.getMaxStackSize(targetStack)) {
                return;
            }
        }
        info.setReturnValue(ItemStack.EMPTY);
    }

}
