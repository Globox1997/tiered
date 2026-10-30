package draylar.tiered.mixin.client;

import draylar.tiered.Tiered;
import draylar.tiered.api.PotentialAttribute;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    @Unique
    private boolean isTiered;
    @Unique
    private boolean slotInfo;
    @Unique
    private Map<Holder<Attribute>, List<AttributeModifier>> tieredMap;

    // Three methods to remove multiple "When Worn",...
    @Inject(method = "addAttributeTooltips", at = @At("HEAD"))
    private void appendAttributeModifiersTooltipMixin(Consumer<Component> textConsumer, @Nullable Player player, CallbackInfo info) {
        ItemStack itemStack = (ItemStack) (Object) this;
        this.isTiered = itemStack.get(Tiered.TIER) != null;
        if (this.isTiered) {
            if (this.tieredMap == null) {
                this.tieredMap = new HashMap<>();
            } else {
                this.tieredMap.clear();
            }

            for (EquipmentSlotGroup attributeModifierSlot : EquipmentSlotGroup.values()) {
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                this.forEachModifier(attributeModifierSlot, (attribute, modifier) -> {
                    List<AttributeModifier> modifiers;
                    if (this.tieredMap.containsKey(attribute)) {
                        modifiers = this.tieredMap.get(attribute);
                        modifiers.add(modifier);
                    } else {
                        modifiers = new ArrayList<>();
                    }
                    if (modifier.amount() > 0.0001D || modifier.amount() < -0.0001D) {
                        this.tieredMap.put(attribute, modifiers);
                    }
                    mutableBoolean.setValue(true);
                });
                if (mutableBoolean.getValue()) {
                    break;
                }
            }
        }
        this.slotInfo = true;
    }

    @Inject(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"), cancellable = true)
    private void appendAttributeModifiersTooltipTwoMixin(Consumer<Component> textConsumer, @Nullable Player player, CallbackInfo info) {
        if (this.isTiered && !this.slotInfo) {
            info.cancel();
        }
    }

    @Inject(method = "method_57370", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private void method_57370Mixin(MutableBoolean mutableBoolean, Consumer<Component> consumer, EquipmentSlotGroup attributeModifierSlot, Player playerEntity, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo info) {
        if (this.isTiered) {
            this.slotInfo = false;
        }
    }

    @Inject(method = "addModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void appendAttributeModifierTooltipMixin(Consumer<Component> textConsumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo info, double d, boolean bl, double e) {
        if (this.isTiered && this.tieredMap.containsKey(attribute)) {
            MutableComponent text = CommonComponents.space();
            text.append(Component.translatable(
                            "tiered.attribute.modifier.equals." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e))
                    .withStyle(ChatFormatting.DARK_GREEN));
            for (int i = 0; i < this.tieredMap.get(attribute).size(); i++) {
                if (this.tieredMap.get(attribute).get(i).is(modifier.id())) {
                    info.cancel();
                    return;
                }
                AttributeModifier tieredModifier = this.tieredMap.get(attribute).get(i);
                double tieredValue;
                if (tieredModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        || tieredModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    tieredValue = tieredModifier.amount() * 100.0;
                } else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
                    tieredValue = tieredModifier.amount() * 10.0;
                } else {
                    tieredValue = tieredModifier.amount();
                }
                boolean addition = tieredValue > 0;
                text.append(CommonComponents.space());
                text.append(Component.translatable("tiered.attribute.modifier", "(" + (addition ? "+" : "") + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(tieredValue) + (tieredModifier.operation().id() > 0 ? "%" : "") + ")").withStyle(addition ? ChatFormatting.DARK_GREEN : ChatFormatting.RED));
            }
            text.append(CommonComponents.space());
            text.append(Component.translatable(attribute.value().getDescriptionId()).withStyle(ChatFormatting.DARK_GREEN));
            textConsumer.accept(text);
            info.cancel();
        }
    }

    @Inject(method = "addModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void appendAttributeModifierTooltipTwoMixin(Consumer<Component> textConsumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo info, double d, boolean bl, double e) {
        if (this.isTiered && this.tieredMap.containsKey(attribute)) {
            MutableComponent text = Component.translatable(
                            "tiered.attribute.modifier.plus." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e))
                    .withStyle(attribute.value().getStyle(true));
            for (int i = 0; i < this.tieredMap.get(attribute).size(); i++) {
                if (this.tieredMap.get(attribute).get(i).is(modifier.id())) {
                    info.cancel();
                    return;
                }

                AttributeModifier tieredModifier = this.tieredMap.get(attribute).get(i);
                double tieredValue;
                if (tieredModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        || tieredModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    tieredValue = tieredModifier.amount() * 100.0;
                } else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
                    tieredValue = tieredModifier.amount() * 10.0;
                } else {
                    tieredValue = tieredModifier.amount();
                }
                boolean addition = tieredValue > 0;
                text.append(CommonComponents.space());
                text.append(Component.translatable("tiered.attribute.modifier", "(" + (addition ? "+" : "") + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(tieredValue) + (tieredModifier.operation().id() > 0 ? "%" : "") + ")").withStyle(addition ? ChatFormatting.BLUE : ChatFormatting.RED));
            }
            text.append(CommonComponents.space());
            text.append(Component.translatable(attribute.value().getDescriptionId()).withStyle(attribute.value().getStyle(true)));
            textConsumer.accept(text);
            info.cancel();
        }
    }

    @Inject(method = "addModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void appendAttributeModifierTooltipThreeMixin(Consumer<Component> textConsumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo info, double d, boolean bl, double e) {
        if (this.isTiered && this.tieredMap.containsKey(attribute)) {
            MutableComponent text = Component.translatable(
                            "tiered.attribute.modifier.take." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-e))
                    .withStyle(attribute.value().getStyle(false));
            for (int i = 0; i < this.tieredMap.get(attribute).size(); i++) {
                if (this.tieredMap.get(attribute).get(i).is(modifier.id())) {
                    info.cancel();
                    return;
                }

                AttributeModifier tieredModifier = this.tieredMap.get(attribute).get(i);
                double tieredValue;
                if (tieredModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        || tieredModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    tieredValue = tieredModifier.amount() * 100.0;
                } else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
                    tieredValue = tieredModifier.amount() * 10.0;
                } else {
                    tieredValue = tieredModifier.amount();
                }
                boolean addition = tieredValue > 0;
                text.append(CommonComponents.space());
                text.append(Component.translatable("tiered.attribute.modifier", "(" + (addition ? "+" : "") + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(tieredValue) + (tieredModifier.operation().id() > 0 ? "%" : "") + ")").withStyle(addition ? ChatFormatting.BLUE : ChatFormatting.RED));
            }
            text.append(CommonComponents.space());
            text.append(Component.translatable(attribute.value().getDescriptionId()).withStyle(ChatFormatting.RED));
            textConsumer.accept(text);
            info.cancel();
        }
    }

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void getNameMixin(CallbackInfoReturnable<Component> info) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.get(Tiered.TIER) != null) {
            // attempt to display attribute if it is valid
            ResourceLocation tierId = ResourceLocation.tryParse(stack.get(Tiered.TIER).tier());
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
    public abstract void forEachModifier(EquipmentSlotGroup slot, BiConsumer<Holder<Attribute>, AttributeModifier> attributeModifierConsumer);
}
