package draylar.tiered.mixin.client;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import draylar.tiered.Tiered;
import draylar.tiered.api.PotentialAttribute;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    @Shadow
    public abstract NbtCompound getOrCreateSubNbt(String key);

    @Shadow
    public abstract boolean hasNbt();

    @Shadow
    public abstract NbtCompound getSubNbt(String key);

    @Shadow
    @Final
    @Mutable
    public static DecimalFormat MODIFIER_FORMAT;

    private boolean isTiered = false;
    private String translationKey;
    private String armorModifierFormat;
    private Map<String, ArrayList> map = new HashMap<>();

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 6), locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeTooltipInformation(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List> info, List list, MutableText mutableText, int i, EquipmentSlot var6[], int var7,
            int var8, EquipmentSlot equipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier> multimap) {
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
            String translationKey = entry.getKey().getTranslationKey();
            if (entry.getValue().getName().contains("tiered:") && !map.containsKey(translationKey) && multimap.get(entry.getKey()).size() > 1) {
                double value = entry.getValue().getValue();
                String format = MODIFIER_FORMAT.format(
                        entry.getValue().getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || entry.getValue().getOperation() == EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                                ? value * 100.0
                                : (entry.getKey().equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? value * 10.0 : value));

                ArrayList collect = new ArrayList<>();
                collect.add(entry.getValue().getOperation().getId()); // Operation Id
                collect.add(format); // Value formated
                collect.add(value > 0.0D); // Value greater 0
                map.put(translationKey, collect);
            }
        }
    }

    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 8))
    private boolean modifyTooltipPlus(List<Text> list, Object text) {
        String translationKey = this.translationKey;
        if (this.map != null && !this.map.isEmpty() && this.map.containsKey(translationKey)) {
            if (!this.isTiered) {
                ArrayList collected = map.get(translationKey);
                list.add(Text.translatable("tiered.attribute.modifier.plus." + (int) collected.get(0), "§9+" + this.armorModifierFormat,
                        ((boolean) collected.get(2) ? "§9(+" : "§c(") + (String) collected.get(1) + ((int) collected.get(0) > 0 ? "%)" : ")"),
                        Text.translatable(translationKey).formatted(Formatting.BLUE)));
            }
        } else
            list.add((Text) text);
        return true;
    }

    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 9))
    private boolean modifyTooltipTake(List<Text> list, Object text) {
        if (this.map != null && !this.map.isEmpty() && this.map.containsKey(this.translationKey)) {
        } else
            list.add((Text) text);
        return true;
    }

    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 7))
    private boolean modifyTooltipEquals(List<Text> list, Object text) {
        if (this.map != null && !this.map.isEmpty() && this.map.containsKey(this.translationKey)) {
            ArrayList collected = map.get(translationKey);
            list.add(Text.translatable("tiered.attribute.modifier.equals." + (int) collected.get(0), "§2 " + this.armorModifierFormat,
                    ((boolean) collected.get(2) ? "§2(+" : "§c(") + (String) collected.get(1) + ((int) collected.get(0) > 0 ? "%)" : ")"),
                    Text.translatable(translationKey).formatted(Formatting.DARK_GREEN)));
        } else
            list.add((Text) text);
        return true;
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;getOperation()Lnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeAttributeModifier(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List> cir, List list, MutableText mutableText, int i, EquipmentSlot var6[], int var7,
            int var8, EquipmentSlot equipmentSlot, Multimap multimap, Iterator var11, Map.Entry<EntityAttribute, EntityAttributeModifier> entry, EntityAttributeModifier entityAttributeModifier,
            double d) {
        this.isTiered = entityAttributeModifier.getName().contains("tiered:");
        this.translationKey = entry.getKey().getTranslationKey();
        this.armorModifierFormat = MODIFIER_FORMAT.format(
                entityAttributeModifier.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || entityAttributeModifier.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                        ? d * 100.0
                        : (entry.getKey().equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? d * 10.0 : d));
    }

    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;formatted(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;", ordinal = 2))
    private MutableText getFormatting(MutableText text, Formatting formatting) {
        if (this.hasNbt() && this.getSubNbt(Tiered.NBT_SUBTAG_KEY) != null && isTiered) {
            Identifier tier = new Identifier(this.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));
            PotentialAttribute attribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);
            return text.setStyle(attribute.getStyle());
        } else {
            return text.formatted(formatting);
        }
    }

    @ModifyVariable(method = "getTooltip", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z"), index = 10)
    private Multimap<EntityAttribute, EntityAttributeModifier> sort(Multimap<EntityAttribute, EntityAttributeModifier> map) {
        Multimap<EntityAttribute, EntityAttributeModifier> vanillaFirst = LinkedListMultimap.create();
        Multimap<EntityAttribute, EntityAttributeModifier> remaining = LinkedListMultimap.create();

        map.forEach((entityAttribute, entityAttributeModifier) -> {
            if (!entityAttributeModifier.getName().contains("tiered"))
                vanillaFirst.put(entityAttribute, entityAttributeModifier);
            else
                remaining.put(entityAttribute, entityAttributeModifier);
        });

        vanillaFirst.putAll(remaining);
        return vanillaFirst;
    }

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void getTooltipMixin(CallbackInfoReturnable<List<Text>> returnable) {
        if (this.getSubNbt(Tiered.NBT_SUBTAG_KEY) != null) {
            Identifier tierId = new Identifier(getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));

            // attempt to display attribute if it is valid
            PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId);

            if (potentialAttribute != null) {
                List<Text> tooltip = returnable.getReturnValue();
                Text tier = Text.translatable(potentialAttribute.getID() + ".label").setStyle(potentialAttribute.getStyle());
                tooltip.add(1, Text.translatable("tiered.tooltip.tier", tier).setStyle(Style.EMPTY.withColor(TextColor.parse("gray"))));
                returnable.setReturnValue(tooltip);
            }
        }
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void getNameMixin(CallbackInfoReturnable<Text> info) {
        if (this.getSubNbt(Tiered.NBT_SUBTAG_KEY) != null) {
            Identifier tier = new Identifier(getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));

            // attempt to display attribute if it is valid
            PotentialAttribute potentialAttribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);

            if (potentialAttribute != null)
                info.setReturnValue(Text.empty().append(info.getReturnValue()).setStyle(potentialAttribute.getStyle()));
        }
    }
}
