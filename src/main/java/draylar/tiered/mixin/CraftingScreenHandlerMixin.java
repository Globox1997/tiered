package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"), cancellable = true)
    public void quickMoveMixin(PlayerEntity player, int slot, CallbackInfoReturnable<ItemStack> info) {
        ItemStack result = ((ScreenHandler) (Object) this).getSlot(slot).getStack();
        if (player.getInventory().getEmptySlot() == -1 && player.getInventory().getOccupiedSlotWithRoomForStack(result) == -1) {
            info.setReturnValue(ItemStack.EMPTY);
        }

    }

}
