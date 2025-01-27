package draylar.tiered.mixin.client;

import draylar.tiered.Tiered;
import draylar.tiered.api.PotentialAttribute;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
    private Map<RegistryEntry<EntityAttribute>, List<EntityAttributeModifier>> tieredMap = new HashMap<>();

    // Three methods to remove multiple "When Worn",...
    @Inject(method = "appendAttributeModifiersTooltip", at = @At("HEAD"))
    private void appendAttributeModifiersTooltipMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo info) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.get(Tiered.TIER) != null) {
            this.isTiered = true;
            this.tieredMap.clear();

            for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                this.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
                    List<EntityAttributeModifier> modifiers;
                    if (this.tieredMap.containsKey(attribute)) {
                        modifiers = this.tieredMap.get(attribute);
                        modifiers.add(modifier);
                    } else {
                        modifiers = new ArrayList<>();
                    }
                    if (modifier.value() > 0.0001D || modifier.value() < -0.0001D) {
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

    @Inject(method = "appendAttributeModifiersTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V"), cancellable = true)
    private void appendAttributeModifiersTooltipTwoMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo info) {
        if (this.isTiered && !this.slotInfo) {
            info.cancel();
        }
    }

    @Inject(method = "method_57370", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private void method_57370Mixin(MutableBoolean mutableBoolean, Consumer<Text> consumer, AttributeModifierSlot attributeModifierSlot, PlayerEntity playerEntity, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo info) {
        if (this.isTiered) {
            this.slotInfo = false;
        }
    }

    @Inject(method = "appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void appendAttributeModifierTooltipMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo info, double d, boolean bl, double e) {
        if (this.isTiered && this.tieredMap.containsKey(attribute)) {
            MutableText text = ScreenTexts.space();
            text.append(Text.translatable(
                            "tiered.attribute.modifier.equals." + modifier.operation().getId(),
                            AttributeModifiersComponent.DECIMAL_FORMAT.format(e))
                    .formatted(Formatting.DARK_GREEN));
            for (int i = 0; i < this.tieredMap.get(attribute).size(); i++) {
                if (this.tieredMap.get(attribute).get(i).idMatches(modifier.id())) {
                    info.cancel();
                    return;
                }
                EntityAttributeModifier tieredModifier = this.tieredMap.get(attribute).get(i);
                double tieredValue;
                if (tieredModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        || tieredModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    tieredValue = tieredModifier.value() * 100.0;
                } else if (attribute.matches(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                    tieredValue = tieredModifier.value() * 10.0;
                } else {
                    tieredValue = tieredModifier.value();
                }
                boolean addition = tieredValue > 0;
                text.append(ScreenTexts.space());
                text.append(Text.translatable("tiered.attribute.modifier", "(" + (addition ? "+" : "") + AttributeModifiersComponent.DECIMAL_FORMAT.format(tieredValue) + (tieredModifier.operation().getId() > 0 ? "%" : "") + ")").formatted(addition ? Formatting.DARK_GREEN : Formatting.RED));
            }
            text.append(ScreenTexts.space());
            text.append(Text.translatable(attribute.value().getTranslationKey()).formatted(Formatting.DARK_GREEN));
            textConsumer.accept(text);
            info.cancel();
        }
    }

    @Inject(method = "appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void appendAttributeModifierTooltipTwoMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo info, double d, boolean bl, double e) {
        if (this.isTiered && this.tieredMap.containsKey(attribute)) {
            MutableText text = Text.translatable(
                            "tiered.attribute.modifier.plus." + modifier.operation().getId(),
                            AttributeModifiersComponent.DECIMAL_FORMAT.format(e))
                    .formatted(attribute.value().getFormatting(true));
            for (int i = 0; i < this.tieredMap.get(attribute).size(); i++) {
                if (this.tieredMap.get(attribute).get(i).idMatches(modifier.id())) {
                    info.cancel();
                    return;
                }

                EntityAttributeModifier tieredModifier = this.tieredMap.get(attribute).get(i);
                double tieredValue;
                if (tieredModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        || tieredModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    tieredValue = tieredModifier.value() * 100.0;
                } else if (attribute.matches(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                    tieredValue = tieredModifier.value() * 10.0;
                } else {
                    tieredValue = tieredModifier.value();
                }
                boolean addition = tieredValue > 0;
                text.append(ScreenTexts.space());
                text.append(Text.translatable("tiered.attribute.modifier", "(" + (addition ? "+" : "") + AttributeModifiersComponent.DECIMAL_FORMAT.format(tieredValue) + (tieredModifier.operation().getId() > 0 ? "%" : "") + ")").formatted(addition ? Formatting.BLUE : Formatting.RED));
            }
            text.append(ScreenTexts.space());
            text.append(Text.translatable(attribute.value().getTranslationKey()).formatted(attribute.value().getFormatting(true)));
            textConsumer.accept(text);
            info.cancel();
        }
    }

    @Inject(method = "appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void appendAttributeModifierTooltipThreeMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo info, double d, boolean bl, double e) {
        if (this.isTiered && this.tieredMap.containsKey(attribute)) {
            MutableText text = Text.translatable(
                            "tiered.attribute.modifier.take." + modifier.operation().getId(),
                            AttributeModifiersComponent.DECIMAL_FORMAT.format(-e))
                    .formatted(attribute.value().getFormatting(false));
            for (int i = 0; i < this.tieredMap.get(attribute).size(); i++) {
                if (this.tieredMap.get(attribute).get(i).idMatches(modifier.id())) {
                    info.cancel();
                    return;
                }

                EntityAttributeModifier tieredModifier = this.tieredMap.get(attribute).get(i);
                double tieredValue;
                if (tieredModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        || tieredModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    tieredValue = tieredModifier.value() * 100.0;
                } else if (attribute.matches(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
                    tieredValue = tieredModifier.value() * 10.0;
                } else {
                    tieredValue = tieredModifier.value();
                }
                boolean addition = tieredValue > 0;
                text.append(ScreenTexts.space());
                text.append(Text.translatable("tiered.attribute.modifier", "(" + (addition ? "+" : "") + AttributeModifiersComponent.DECIMAL_FORMAT.format(tieredValue) + (tieredModifier.operation().getId() > 0 ? "%" : "") + ")").formatted(addition ? Formatting.BLUE : Formatting.RED));
            }
            text.append(ScreenTexts.space());
            text.append(Text.translatable(attribute.value().getTranslationKey()).formatted(Formatting.RED));
            textConsumer.accept(text);
            info.cancel();
        }
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void getNameMixin(CallbackInfoReturnable<Text> info) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.get(Tiered.TIER) != null) {
            // attempt to display attribute if it is valid
            if (Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().containsKey(Identifier.of(stack.get(Tiered.TIER).tier()))) {
                PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(Identifier.of(stack.get(Tiered.TIER).tier()));

                if (potentialAttribute != null) {
                    info.setReturnValue(Text.translatable(potentialAttribute.getID() + ".label").append(" ").append(info.getReturnValue()).setStyle(potentialAttribute.getStyle()));
                }
            }
        }
    }

    @Shadow
    public abstract void applyAttributeModifier(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer);
}
