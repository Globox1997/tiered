package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"), cancellable = true)
    public void quickMoveMixin(PlayerEntity player, int slot, CallbackInfoReturnable<ItemStack> info) {
        ScreenHandler screenHandler = (ScreenHandler) (Object) this;
        ItemStack result = screenHandler.getSlot(slot).getStack();
        for (int i = 10; i < 46; i++) {
            Slot targetSlot = screenHandler.getSlot(i);
            ItemStack targetStack = targetSlot.getStack();
            if (targetStack.isEmpty() ? targetSlot.canInsert(result)
                    : ItemStack.areItemsAndComponentsEqual(targetStack, result) && targetStack.getCount() < targetSlot.getMaxItemCount(targetStack)) {
                return;
            }
        }
        info.setReturnValue(ItemStack.EMPTY);
    }

}
