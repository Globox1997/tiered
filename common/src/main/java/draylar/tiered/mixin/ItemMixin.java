package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void onCraftByPlayerMixin(ItemStack stack, Player player, CallbackInfo info) {
        if (!player.level().isClientSide() && !stack.isEmpty()) {
            boolean enabled = player.containerMenu instanceof MerchantMenu ? ConfigInit.CONFIG.merchantModifier : ConfigInit.CONFIG.craftingModifier;
            if (enabled) {
                ModifierUtils.setItemStackAttribute(player, stack, false);
            }
        }
    }

}
