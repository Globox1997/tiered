package draylar.tiered.mixin;

import draylar.tiered.Tiered;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.api.TierComponent;
import draylar.tiered.config.ConfigInit;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getMaxDamage", at = @At("TAIL"), cancellable = true)
    private void getMaxDamageMixin(CallbackInfoReturnable<Integer> info) {
        ItemStack stack = (ItemStack) (Object) this;
        TierComponent tier = stack.get(Tiered.TIER);
        int base = info.getReturnValue();
        if (tier != null && tier.hasDurableFactor() && base > 0) {
            double change = tier.operation() == 0 ? tier.durable() : (double) base * tier.durable();
            double maxDamage = base + (double) (long) change;
            info.setReturnValue((int) Mth.clamp(maxDamage, 1.0D, (double) Integer.MAX_VALUE));
        }
    }

    @Inject(method = "onCraftedBySystem", at = @At("TAIL"))
    private void onCraftByCrafterMixin(Level world, CallbackInfo info) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!world.isClientSide() && !stack.isEmpty() && ConfigInit.CONFIG.craftingModifier) {
            ModifierUtils.setItemStackAttribute(null, stack, false);
        }
    }

    // Used for tooltip
    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"))
    private void applyAttributeModifierMixin(EquipmentSlotGroup slot, BiConsumer<Holder<Attribute>, AttributeModifier> attributeModifierConsumer, CallbackInfo info) {
        applyAttributeModifier(null, slot, attributeModifierConsumer);
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"))
    private void applyAttributeModifiersMixin(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> attributeModifierConsumer, CallbackInfo info) {
        applyAttributeModifier(slot, null, attributeModifierConsumer);
    }

    private void applyAttributeModifier(@Nullable EquipmentSlot equipmentSlot, @Nullable EquipmentSlotGroup attributeModifierSlot, BiConsumer<Holder<Attribute>, AttributeModifier> attributeModifierConsumer) {
        ModifierUtils.applyTierModifiers(Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes(), (ItemStack) (Object) this, equipmentSlot, attributeModifierSlot, attributeModifierConsumer);
    }
}
