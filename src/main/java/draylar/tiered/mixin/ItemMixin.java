package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import draylar.tiered.Tiered;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "onCraftByPlayer", at = @At("HEAD"))
    private void onCraftByPlayerMixin(ItemStack stack, World world, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient() && !stack.isEmpty()) {
            boolean enabled = player.currentScreenHandler instanceof MerchantScreenHandler ? ConfigInit.CONFIG.merchantModifier : ConfigInit.CONFIG.craftingModifier;
            if (enabled) {
                ModifierUtils.setItemStackAttribute(player, stack, false);
            }
        }
    }

    @Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
    private void getItemBarStepMixin(ItemStack stack, CallbackInfoReturnable<Integer> info) {
        if (stack.get(Tiered.TIER) != null && stack.get(Tiered.TIER).hasDurableFactor()) {
            info.setReturnValue(MathHelper.clamp(Math.round(13.0f - (float) stack.getDamage() * 13.0f / (float) stack.getMaxDamage()), 0, 13));
        }
    }

    @Inject(method = "getItemBarColor", at = @At("HEAD"), cancellable = true)
    private void getItemBarColorMixin(ItemStack stack, CallbackInfoReturnable<Integer> info) {
        if (stack.get(Tiered.TIER) != null && stack.get(Tiered.TIER).hasDurableFactor()) {
            float f = Math.max(0.0f, ((float) stack.getMaxDamage() - (float) stack.getDamage()) / (float) stack.getMaxDamage());
            info.setReturnValue(MathHelper.hsvToRgb(f / 3.0f, 1.0f, 1.0f));
        }
    }

}
