package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import draylar.tiered.Tiered;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void onCraftByPlayerMixin(ItemStack stack, Level world, Player player, CallbackInfo info) {
        if (!world.isClientSide() && !stack.isEmpty()) {
            boolean enabled = player.containerMenu instanceof MerchantMenu ? ConfigInit.CONFIG.merchantModifier : ConfigInit.CONFIG.craftingModifier;
            if (enabled) {
                ModifierUtils.setItemStackAttribute(player, stack, false);
            }
        }
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void getItemBarStepMixin(ItemStack stack, CallbackInfoReturnable<Integer> info) {
        if (stack.get(Tiered.TIER) != null && stack.get(Tiered.TIER).hasDurableFactor()) {
            info.setReturnValue(Mth.clamp(Math.round(13.0f - (float) stack.getDamageValue() * 13.0f / (float) stack.getMaxDamage()), 0, 13));
        }
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void getItemBarColorMixin(ItemStack stack, CallbackInfoReturnable<Integer> info) {
        if (stack.get(Tiered.TIER) != null && stack.get(Tiered.TIER).hasDurableFactor()) {
            float f = Math.max(0.0f, ((float) stack.getMaxDamage() - (float) stack.getDamageValue()) / (float) stack.getMaxDamage());
            info.setReturnValue(Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f));
        }
    }

}
