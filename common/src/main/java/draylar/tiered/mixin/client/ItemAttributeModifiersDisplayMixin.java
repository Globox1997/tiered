package draylar.tiered.mixin.client;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import draylar.tiered.util.TieredAttributeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(ItemAttributeModifiers.Display.Default.class)
public class ItemAttributeModifiersDisplayMixin {

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0), cancellable = true)
    private void appendAttributeModifierTooltipMixin(Consumer<Component> textConsumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo info,
            @Local(ordinal = 1) double e) {
        Map<Holder<Attribute>, List<AttributeModifier>> tieredMap = TieredAttributeTooltip.tieredMap;
        if (tieredMap != null && tieredMap.containsKey(attribute)) {
            MutableComponent text = CommonComponents.space();
            text.append(Component.translatable(
                            "tiered.attribute.modifier.equals." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e))
                    .withStyle(ChatFormatting.DARK_GREEN));
            for (int i = 0; i < tieredMap.get(attribute).size(); i++) {
                if (tieredMap.get(attribute).get(i).is(modifier.id())) {
                    info.cancel();
                    return;
                }
                AttributeModifier tieredModifier = tieredMap.get(attribute).get(i);
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

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1), cancellable = true)
    private void appendAttributeModifierTooltipTwoMixin(Consumer<Component> textConsumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo info,
            @Local(ordinal = 1) double e) {
        Map<Holder<Attribute>, List<AttributeModifier>> tieredMap = TieredAttributeTooltip.tieredMap;
        if (tieredMap != null && tieredMap.containsKey(attribute)) {
            MutableComponent text = Component.translatable(
                            "tiered.attribute.modifier.plus." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e))
                    .withStyle(attribute.value().getStyle(true));
            for (int i = 0; i < tieredMap.get(attribute).size(); i++) {
                if (tieredMap.get(attribute).get(i).is(modifier.id())) {
                    info.cancel();
                    return;
                }
                AttributeModifier tieredModifier = tieredMap.get(attribute).get(i);
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

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 2), cancellable = true)
    private void appendAttributeModifierTooltipThreeMixin(Consumer<Component> textConsumer, @Nullable Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo info,
            @Local(ordinal = 1) double e) {
        Map<Holder<Attribute>, List<AttributeModifier>> tieredMap = TieredAttributeTooltip.tieredMap;
        if (tieredMap != null && tieredMap.containsKey(attribute)) {
            MutableComponent text = Component.translatable(
                            "tiered.attribute.modifier.take." + modifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-e))
                    .withStyle(attribute.value().getStyle(false));
            for (int i = 0; i < tieredMap.get(attribute).size(); i++) {
                if (tieredMap.get(attribute).get(i).is(modifier.id())) {
                    info.cancel();
                    return;
                }
                AttributeModifier tieredModifier = tieredMap.get(attribute).get(i);
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

}
