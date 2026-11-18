package draylar.tiered.mixin.client;

import draylar.tiered.Tiered;
import draylar.tiered.api.PotentialAttribute;
import draylar.tiered.util.TieredAttributeTooltip;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    // Three methods to remove multiple "When Worn",...
    @Inject(method = "addAttributeTooltips", at = @At("HEAD"))
    private void appendAttributeModifiersTooltipMixin(Consumer<Component> textConsumer, TooltipDisplay display, @Nullable Player player, CallbackInfo info) {
        ItemStack itemStack = (ItemStack) (Object) this;
        TieredAttributeTooltip.tieredMap = null;
        if (itemStack.get(Tiered.TIER) != null) {
            Map<Holder<Attribute>, List<AttributeModifier>> tieredMap = new HashMap<>();

            for (EquipmentSlotGroup attributeModifierSlot : EquipmentSlotGroup.values()) {
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                this.forEachModifier(attributeModifierSlot, (attribute, modifier, tooltip) -> {
                    if (tooltip == ItemAttributeModifiers.Display.hidden()) {
                        return;
                    }
                    List<AttributeModifier> modifiers;
                    if (tieredMap.containsKey(attribute)) {
                        modifiers = tieredMap.get(attribute);
                        modifiers.add(modifier);
                    } else {
                        modifiers = new ArrayList<>();
                    }
                    if (modifier.amount() > 0.0001D || modifier.amount() < -0.0001D) {
                        tieredMap.put(attribute, modifiers);
                    }
                    mutableBoolean.setValue(true);
                });
                if (mutableBoolean.getValue()) {
                    break;
                }
            }
            TieredAttributeTooltip.tieredMap = tieredMap;
        }
        TieredAttributeTooltip.slotInfo = true;
    }

    @Inject(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V"), cancellable = true)
    private void appendAttributeModifiersTooltipTwoMixin(Consumer<Component> textConsumer, TooltipDisplay display, @Nullable Player player, CallbackInfo info) {
        if (TieredAttributeTooltip.tieredMap != null && !TieredAttributeTooltip.slotInfo) {
            TieredAttributeTooltip.tieredMap = null;
            info.cancel();
        }
    }

    @Inject(method = "addAttributeTooltips", at = @At("RETURN"))
    private void appendAttributeModifiersTooltipReturnMixin(Consumer<Component> textConsumer, TooltipDisplay display, @Nullable Player player, CallbackInfo info) {
        TieredAttributeTooltip.tieredMap = null;
    }

    @Inject(method = "lambda$addAttributeTooltips$0", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private static void addAttributeTooltipsLambdaMixin(MutableBoolean mutableBoolean, Consumer<Component> consumer, EquipmentSlotGroup attributeModifierSlot, Player playerEntity, Holder<Attribute> attribute,
            AttributeModifier modifier, ItemAttributeModifiers.Display tooltip, CallbackInfo info) {
        if (TieredAttributeTooltip.tieredMap != null) {
            TieredAttributeTooltip.slotInfo = false;
        }
    }

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void getNameMixin(CallbackInfoReturnable<Component> info) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.get(Tiered.TIER) != null) {
            // attempt to display attribute if it is valid
            Identifier tierId = Identifier.tryParse(stack.get(Tiered.TIER).tier());
            if (tierId != null && Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().containsKey(tierId)) {
                PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);

                if (potentialAttribute != null) {
                    MutableComponent name = Component.translatable(potentialAttribute.getID() + ".label").append(" ").append(info.getReturnValue());
                    info.setReturnValue(potentialAttribute.getStyle() != null ? name.setStyle(potentialAttribute.getStyle()) : name);
                }
            }
        }
    }

    @Shadow
    public abstract void forEachModifier(EquipmentSlotGroup slot, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer);
}
