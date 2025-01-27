package draylar.tiered.mixin;

import draylar.tiered.Tiered;
import draylar.tiered.api.AttributeTemplate;
import draylar.tiered.api.ModifierUtils;
import draylar.tiered.api.PotentialAttribute;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getMaxDamage", at = @At("TAIL"), cancellable = true)
    private void getMaxDamageMixin(CallbackInfoReturnable<Integer> info) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.get(Tiered.TIER) != null && stack.get(Tiered.TIER).durable() > 0) {
            if (stack.get(Tiered.TIER).operation() == 0) {
                info.setReturnValue(info.getReturnValue() + (int) stack.get(Tiered.TIER).durable());
            } else {
                info.setReturnValue(info.getReturnValue() + (int) ((float) info.getReturnValue() * stack.get(Tiered.TIER).durable()));
            }
        }
    }

    // Used for tooltip
    @Inject(method = "Lnet/minecraft/item/ItemStack;applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V"))
    private void applyAttributeModifierMixin(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo info) {
        applyAttributeModifier(null, slot, attributeModifierConsumer);
    }

    @Inject(method = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"))
    private void applyAttributeModifiersMixin(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo info) {
        applyAttributeModifier(slot, null, attributeModifierConsumer);
    }

    private void applyAttributeModifier(@Nullable EquipmentSlot equipmentSlot, @Nullable AttributeModifierSlot attributeModifierSlot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.get(Tiered.TIER) != null) {
            Identifier tier = ModifierUtils.getAttributeId(itemStack);
            PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);
            if (potentialAttribute != null) {
                for (AttributeTemplate template : potentialAttribute.getAttributes()) {
                    // get required equipment slots
                    if (template.getRequiredEquipmentSlots() != null) {
                        List<EquipmentSlot> requiredEquipmentSlots = new ArrayList<>(Arrays.asList(template.getRequiredEquipmentSlots()));

                        if (equipmentSlot != null && requiredEquipmentSlots.contains(equipmentSlot)) {
                            template.applyModifiers(equipmentSlot, attributeModifierConsumer);
                        } else if (attributeModifierSlot != null) {
                            Optional<EquipmentSlot> optional = Arrays.stream(template.getRequiredEquipmentSlots()).filter(attributeModifierSlot::matches).findFirst();
                            if (optional.isPresent() && Tiered.isPreferredEquipmentSlot(itemStack, optional.get())) {
                                template.applyModifiers(optional.get(), attributeModifierConsumer);
                            }
                        }
                    }
                    // get optional equipment slots
                    if (template.getOptionalEquipmentSlots() != null) {
                        List<EquipmentSlot> optionalEquipmentSlots = new ArrayList<>(Arrays.asList(template.getOptionalEquipmentSlots()));
                        // optional equipment slots are valid ONLY IF the equipment slot is valid for the thing
                        if (equipmentSlot != null && optionalEquipmentSlots.contains(equipmentSlot) && Tiered.isPreferredEquipmentSlot(itemStack, equipmentSlot)) {
                            template.applyModifiers(equipmentSlot, attributeModifierConsumer);
                        } else if (attributeModifierSlot != null && attributeModifierSlot != AttributeModifierSlot.ANY && attributeModifierSlot != AttributeModifierSlot.HAND) {
                            Optional<EquipmentSlot> optional = Arrays.stream(template.getOptionalEquipmentSlots()).filter(attributeModifierSlot::matches).findFirst();
                            if (optional.isPresent() && Tiered.isPreferredEquipmentSlot(itemStack, optional.get())) {
                                template.applyModifiers(optional.get(), attributeModifierConsumer);
                            }
                        }
                    }
                }
            }
        }
    }
}
