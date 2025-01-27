package draylar.tiered.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import draylar.tiered.Tiered;
import draylar.tiered.api.PotentialAttribute;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
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

//@SuppressWarnings({"rawtypes", "unchecked"})
@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {
//    //
//    @Unique
//    private boolean isTiered = false;
//    //    @Unique
////    private String translationKey;
////    @Unique
////    private String armorModifierFormat;
//    @Unique
//    private Map<String, ArrayList<Object>> tieredMap = new HashMap<>();
//    @Unique
//    private List<String> existingModifiers = new ArrayList<>();

    @Unique
    private boolean slotInfo;

    // Three methods to remove multiple "When Worn",...
    @Inject(method = "appendAttributeModifiersTooltip", at = @At("HEAD"))
    private void appendAttributeModifiersTooltipMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo info) {
        this.slotInfo = true;
    }

    @Inject(method = "appendAttributeModifiersTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V"), cancellable = true)
    private void appendAttributeModifiersTooltipMixinT(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo info) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.get(Tiered.TIER) != null && !this.slotInfo) {
            info.cancel();
        }
    }

    @Inject(method = "method_57370", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private void appendAttributeModifiersTooltipMixin(MutableBoolean mutableBoolean, Consumer<Text> consumer, AttributeModifierSlot attributeModifierSlot, PlayerEntity playerEntity, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo info) {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.get(Tiered.TIER) != null) {
            this.slotInfo = false;
        }
    }

//    private List<Object> list = new ArrayList<>();
//    @Unique
//    private boolean toughnessZero = false;

    // @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 6), locals = LocalCapture.PRINT)
    // private void storeTooltipInformation(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info, List list, MutableText mutableText,
    //         int i, EquipmentSlot var6[], int var7, int var8, EquipmentSlot equipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier> multimap) {
    //     // for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
    //     //     String translationKey = entry.getKey().getTranslationKey();
    //     //     if (entry.getValue().getName().contains("tiered:") && !map.containsKey(translationKey) && multimap.get(entry.getKey()).size() > 1) {
    //     //         double value = entry.getValue().getValue();
    //     //         String format = MODIFIER_FORMAT.format(
    //     //                 entry.getValue().getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || entry.getValue().getOperation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    //     //                         ? value * 100.0
    //     //                         : (entry.getKey().equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? value * 10.0 : value));

    //     //         ArrayList collect = new ArrayList<>();
    //     //         collect.add(entry.getValue().getOperation().getId()); // Operation Id
    //     //         collect.add(format); // Value formated
    //     //         collect.add(value > 0.0D); // Value greater 0
    //     //         map.put(translationKey, collect);
    //     //     }
    //     // }
    // }


    // @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;getOperation()Lnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    // private void storeAttributeModifier(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List> info, List list, MutableText mutableText, int i,
    //         EquipmentSlot var6[], int var7, int var8, EquipmentSlot equipmentSlot, Multimap multimap, Iterator var11, Map.Entry<EntityAttribute, EntityAttributeModifier> entry,
    //         EntityAttributeModifier entityAttributeModifier, double d) {
    //     this.isTiered = entityAttributeModifier.getName().contains("tiered:");
    //     this.translationKey = entry.getKey().getTranslationKey();
    //     this.armorModifierFormat = MODIFIER_FORMAT.format(
    //             entityAttributeModifier.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || entityAttributeModifier.getOperation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    //                     ? d * 100.0
    //                     : (entry.getKey().equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? d * 10.0 : d));

    //     // special case
    //     if (entry.getKey().equals(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)) {
    //         if (this.isTiered) {
    //             if (this.toughnessZero) {
    //                 ArrayList collected = map.get(translationKey);
    //                 // plus
    //                 if ((boolean) collected.get(2)) {
    //                     list.add(Text.translatable("tiered.attribute.modifier.plus." + (int) collected.get(0), "§9+" + this.armorModifierFormat,
    //                             Text.translatable(translationKey).formatted(Formatting.BLUE), ""));
    //                 } else {
    //                     // take
    //                     list.add(Text.translatable("tiered.attribute.modifier.take." + (int) collected.get(0), "§c" + this.armorModifierFormat,
    //                             Text.translatable(translationKey).formatted(Formatting.RED), ""));
    //                 }
    //             }
    //         } else {
    //             this.toughnessZero = entityAttributeModifier.getValue() < 0.0001D;
    //         }
    //     }

    // }

    // @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;formatted(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;", ordinal = 2))
    // private MutableText getFormatting(MutableText text, Formatting formatting) {
    //     if (this.hasNbt() && this.getSubNbt(Tiered.NBT_SUBTAG_KEY) != null && isTiered) {
    //         Identifier tier = Identifier.of(this.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));
    //         PotentialAttribute attribute = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier);
    //         return text.setStyle(attribute.getStyle());
    //     } else {
    //         return text.formatted(formatting);
    //     }
    // }

    // @ModifyVariable(method = "getTooltip", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;isEmpty()Z"), index = 10)
    // private Multimap<EntityAttribute, EntityAttributeModifier> sort(Multimap<EntityAttribute, EntityAttributeModifier> map) {
    //     Multimap<EntityAttribute, EntityAttributeModifier> vanillaFirst = LinkedListMultimap.create();
    //     Multimap<EntityAttribute, EntityAttributeModifier> remaining = LinkedListMultimap.create();

    //     map.forEach((entityAttribute, entityAttributeModifier) -> {
    //         if (!entityAttributeModifier.getName().contains("tiered")) {
    //             vanillaFirst.put(entityAttribute, entityAttributeModifier);
    //         } else {
    //             remaining.put(entityAttribute, entityAttributeModifier);
    //         }
    //     });

    //     vanillaFirst.putAll(remaining);
    //     return vanillaFirst;
    // }

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

//    private void appendAttributeModifiersTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player) {
//        AttributeModifiersComponent attributeModifiersComponent = this.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
//        if (attributeModifiersComponent.showInTooltip()) {
//            for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
//                MutableBoolean mutableBoolean = new MutableBoolean(true);
//                this.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
//                    if (mutableBoolean.isTrue()) {
//                        textConsumer.accept(ScreenTexts.EMPTY);
//                        textConsumer.accept(Text.translatable("item.modifiers." + attributeModifierSlot.asString()).formatted(Formatting.GRAY));
//                        mutableBoolean.setFalse();
//                    }
//
//                    this.appendAttributeModifierTooltip(textConsumer, player, attribute, modifier);
//                });
//            }
//        }
//    }

    // TEST STAAAAAAAAAAAAAAAAAAAART

//    @WrapOperation(method = "method_57370", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V"))
//    private void method_57370Mixin(ItemStack instance, Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, Operation<Void> original) {
//        if (this.isTiered) {
//
//            if (modifier.id().getNamespace().equals("tiered") && this.existingModifiers.contains(attribute.getIdAsString())) {
//                String format = AttributeModifiersComponent.DECIMAL_FORMAT.format(
//                        modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE || modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? modifier.value() * 100.0
//                                : (attribute.equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) ? modifier.value() * 10.0 : modifier.value()));
//
//                ArrayList<Object> collect = new ArrayList<>();
//                collect.add(modifier.operation().getId());
//                collect.add(format); // Value formated
//                collect.add(modifier.value() > 0.0D); // Value greater 0
//                this.tieredMap.put(attribute.value().getTranslationKey(), collect);
//
////                System.out.println("LOL: "+this.tieredMap);
//            } else {
//                this.existingModifiers.add(attribute.getIdAsString());
//            }
////            System.out.println("XXX: "+ this.existingModifiers);
//        } else {
//            original.call(instance, textConsumer, player, attribute, modifier);
//        }
//    }
//
//    //Lnet/minecraft/item/ItemStack;appendAttributeModifiersTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;)V
////    @Inject(method = "appendAttributeModifiersTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/AttributeModifierSlot;values()[Lnet/minecraft/component/type/AttributeModifierSlot;", shift = At.Shift.BY,by = 2))
//    @Inject(method = "appendAttributeModifiersTooltip", at = @At("TAIL"),locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void appendAttributeModifiersTooltipTwoMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo info, AttributeModifiersComponent attributeModifiersComponent   ) {
//
//        if (this.isTiered && attributeModifiersComponent.showInTooltip()) {
////            System.out.println("CHECK "+this.tieredMap);
//            for (AttributeModifierSlot attributeModifierSlot : AttributeModifierSlot.values()) {
//                MutableBoolean mutableBoolean = new MutableBoolean(true);
//                this.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
//                    if (mutableBoolean.isTrue()) {
//                        textConsumer.accept(ScreenTexts.EMPTY);
//                        textConsumer.accept(Text.translatable("item.modifiers." + attributeModifierSlot.asString()).formatted(Formatting.GRAY));
//                        mutableBoolean.setFalse();
//                    }
////                    System.out.println(this.tieredMap.containsKey(attribute.value().getTranslationKey())+ " : "+this.tieredMap);
////                    if(!this.tieredMap.containsKey(attribute.value().getTranslationKey())){
//                    if(!modifier.id().getNamespace().equals("tiered") || !this.tieredMap.containsKey(attribute.value().getTranslationKey()))
//                        this.appendAttributeModifierTooltip(textConsumer, player, attribute, modifier);
////                    }
//                });
//            }
//        }
//    }
//
//
//    @Inject(method = "appendAttributeModifiersTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/AttributeModifiersComponent;showInTooltip()Z", shift = At.Shift.AFTER))
//    private void appendAttributeModifiersTooltipMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo info) {
//        this.isTiered = ((ItemStack) (Object) this).get(Tiered.TIER) != null;
//        this.tieredMap.clear();
//        this.existingModifiers.clear();
////        this.list.clear();
////        System.out.println("CLEAR");
//    }
//
////    @Inject(method = "appendAttributeModifierTooltip", at = @At("HEAD"))
////    private void appendAttributeModifierTooltipMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo info) {
//////   System.out.println(this+" : "+attribute+" : "+modifier);
//////        System.out.println(this.list);
////        //peed]=net.minecraft.entity.attribute.ClampedEntityAttribute@1c5aabe2} : EntityAttributeModifier[id=tiered:uncommon_armor_3, amount=-0.05000000074505806, operation=ADD_MULTIPLIED_TOTAL]
////    }
//
//
//    ///////////////////////
//
//    @Inject(method = "appendAttributeModifierTooltip", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void modifyTooltipPlus(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo info, double d, boolean bl, double e) {
//
////    System.out.println(this.tieredMap+ " : "+attribute.value().getTranslationKey());
//        if (this.isTiered && !this.tieredMap.isEmpty() && this.tieredMap.containsKey(attribute.value().getTranslationKey())) {
////            if (this.map != null && !this.map.isEmpty() && this.map.containsKey(translationKey)) {
////                if (!this.isTiered) {
//                    ArrayList<Object> collected =  this.tieredMap.get(attribute.value().getTranslationKey());
//
////                    System.out.println("TEST");
////System.out.println(Text.translatable("tiered.attribute.modifier.plus." + (int) collected.get(0), "§9+" + (String) collected.get(1),
////        ((boolean) collected.get(2) ? "§9(+" : "§c(") + (String) collected.get(1) + ((int) collected.get(0) > 0 ? "%)" : ")"),
////        Text.translatable(attribute.value().getTranslationKey()).formatted(Formatting.BLUE)));
//
//                    textConsumer.accept(Text.translatable("tiered.attribute.modifier.plus." + (int) collected.get(0), "§9+" + (String) collected.get(1),
//                            ((boolean) collected.get(2) ? "§9(+" : "§c(") + (String) collected.get(1) + ((int) collected.get(0) > 0 ? "%)" : ")"),
//                            Text.translatable(attribute.value().getTranslationKey()).formatted(Formatting.BLUE)));
//
////                    textConsumer.accept(
////                            Text.translatable(
////                                            "tiered.attribute.modifier.plus." + modifier.operation().getId(),
////                                            AttributeModifiersComponent.DECIMAL_FORMAT.format(e),
////                                            Text.translatable(attribute.value().getTranslationKey())
////                                    )
////                                    .formatted(attribute.value().getFormatting(true))
////                    );
//
////                }
////            }
////            textConsumer.accept(ScreenTexts.space().append(Text.translatable(
////                    "tiered.attribute.modifier.plus." + modifier.operation().getId(),
////                    AttributeModifiersComponent.DECIMAL_FORMAT.format(e),
////                    Text.translatable(attribute.value().getTranslationKey()))).formatted(Formatting.BLUE));
////
////            list.add(Text.translatable("tiered.attribute.modifier.plus." + (int) collected.get(0), "§9+" + this.armorModifierFormat,
////                         ((boolean) collected.get(2) ? "§9(+" : "§c(") + (String) collected.get(1) + ((int) collected.get(0) > 0 ? "%)" : ")"),
////                         Text.translatable(translationKey).formatted(Formatting.BLUE)));
//            info.cancel();
//        }
////         String translationKey = this.translationKey;
////         if (this.map != null && !this.map.isEmpty() && this.map.containsKey(translationKey)) {
////             if (!this.isTiered) {
////                 ArrayList collected = map.get(translationKey);
////                 list.add(Text.translatable("tiered.attribute.modifier.plus." + (int) collected.get(0), "§9+" + this.armorModifierFormat,
////                         ((boolean) collected.get(2) ? "§9(+" : "§c(") + (String) collected.get(1) + ((int) collected.get(0) > 0 ? "%)" : ")"),
////                         Text.translatable(translationKey).formatted(Formatting.BLUE)));
////             }
////         } else {
////             list.add((Text) text);
////         }
////         return true;
//    }

    // TEST ENDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
    //
//    @Inject(method = "appendAttributeModifiersTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void appendAttributeModifiersTooltipMixin(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo info, AttributeModifiersComponent attributeModifiersComponent, AttributeModifierSlot[] var4, int var5, int var6, AttributeModifierSlot attributeModifierSlot, MutableBoolean mutableBoolean) {
//
//        if (this.isTiered) {
//
////            List<Object> attributeModifierList = new ArrayList<>();
////
////            for (AttributeModifiersComponent.Entry consumer : attributeModifiersComponent.modifiers()) {
////                System.out.println(this+ " : "+consumer.modifier());
////            }
////            System.out.println(this+ " : "+ attributeModifiersComponent.modifiers());
////            attributeModifiersComponent.
//            this.applyAttributeModifier(attributeModifierSlot, (attribute, modifier) -> {
//                if (mutableBoolean.isTrue()) {
//                    textConsumer.accept(ScreenTexts.EMPTY);
//                    textConsumer.accept(Text.translatable("item.modifiers." + attributeModifierSlot.asString()).formatted(Formatting.GRAY));
//                    mutableBoolean.setFalse();
//                }
//                this.list.add(attribute);
//                this.list.add(modifier);
////                System.out.println("TEST: "+attribute+" : "+modifier);
//                this.appendAttributeModifierTooltip(textConsumer, player, attribute, modifier);
//            });
////            for (int i = 0; i < attributeModifierList.size() / 2; i++) {
////                System.out.println(i+ " : "+((RegistryEntry<EntityAttribute>) attributeModifierList.get(i * 2)).getIdAsString()+ " : "+((EntityAttributeModifier) attributeModifierList.get(i * 2 + 1)).id());
////                //noinspection unchecked
////                this.appendAttributeModifierTooltip(textConsumer, player, (RegistryEntry<EntityAttribute>) attributeModifierList.get(i * 2), (EntityAttributeModifier) attributeModifierList.get(i * 2 + 1));
////            }
////            System.out.println(attributeModifierList);
//            info.cancel();
//        }
//    }

    // @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 9))
    // private boolean modifyTooltipTake(List<Text> list, Object text) {
    //     if (this.map != null && !this.map.isEmpty() && this.map.containsKey(this.translationKey)) {
    //     } else {
    //         list.add((Text) text);
    //     }
    //     return true;
    // }

//     @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 7))
//     private boolean modifyTooltipEquals(List<Text> list, Object text) {
//         if (this.map != null && !this.map.isEmpty() && this.map.containsKey(this.translationKey)) {
//             ArrayList collected = map.get(translationKey);
//             list.add(Text.translatable("tiered.attribute.modifier.equals." + (int) collected.get(0), "§2 " + this.armorModifierFormat,
//                     ((boolean) collected.get(2) ? "§2(+" : "§c(") + (String) collected.get(1) + ((int) collected.get(0) > 0 ? "%)" : ")"),
//                     Text.translatable(translationKey).formatted(Formatting.DARK_GREEN)));
//         } else {
//             list.add((Text) text);
//         }
//         return true;
//     }


    ///////////////////////

    // @Inject(method = "getTooltip", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    // private void getTooltipMixin(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info, List list, MutableText mutableText, int i,
    //         EquipmentSlot var6[], int var7, int var8, EquipmentSlot equipmentSlot, Multimap multimap) {
    //     if (this.isTiered && !multimap.isEmpty() && equipmentSlot == EquipmentSlot.OFFHAND && this.getAttributeModifiers(EquipmentSlot.MAINHAND) != null
    //             && !this.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty()) {
    //         try {
    //             multimap.clear();
    //         } catch (UnsupportedOperationException exception) {
    //         }
    //     }
    // }

    // @ModifyExpressionValue(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", ordinal = 1))
    // private MutableText modifyTooltipEquipmentSlot(MutableText original) {
    //     if (this.isTiered && this.getAttributeModifiers(EquipmentSlot.MAINHAND) != null && !this.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty()
    //             && this.getAttributeModifiers(EquipmentSlot.OFFHAND) != null && !this.getAttributeModifiers(EquipmentSlot.OFFHAND).isEmpty()) {
    //         return Text.translatable("item.modifiers.hand").formatted(Formatting.GRAY);
    //     }
    //     return original;
    // }

    // @Shadow
    // public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
    //     return null;
    // }

//    @Shadow
//    abstract void appendAttributeModifierTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier);
//
//    @Shadow
//    public abstract void applyAttributeModifier(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer);
}
