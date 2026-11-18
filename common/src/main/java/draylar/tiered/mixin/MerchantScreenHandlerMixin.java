package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(MerchantMenu.class)
public abstract class MerchantScreenHandlerMixin extends AbstractContainerMenu {

    public MerchantScreenHandlerMixin(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @ModifyVariable(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z", ordinal = 0), ordinal = 1)
    private ItemStack quickMoveMixin(ItemStack original) {
        if (ConfigInit.CONFIG.merchantModifier) {
            ModifierUtils.setItemStackAttribute(null, original, false);
        }
        return original;
    }
}
