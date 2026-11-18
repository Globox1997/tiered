package draylar.tiered.api;

import draylar.tiered.Tiered;
import draylar.tiered.config.ConfigInit;
import net.libz.util.SortList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

public class ModifierUtils {

    private static final RandomSource RANDOM = RandomSource.createThreadSafe();

    /**
     * Returns the ID of a random attribute that is valid for the given {@link Item} in {@link Identifier} form.
     * <p>
     * If there is no valid attribute for the given {@link Item}, null is returned.
     *
     * @param item {@link Item} to generate a random attribute for
     * @return id of random attribute for item in {@link Identifier} form, or null if there are no valid options
     */
    @Nullable
    public static Identifier getRandomAttributeIDFor(@Nullable Player playerEntity, Item item, boolean reforge) {
        List<Identifier> potentialAttributes = new ArrayList<>();
        List<Integer> attributeWeights = new ArrayList<>();
        // collect all valid attributes for the given item and their weights

        Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
            if (attribute.isValid(BuiltInRegistries.ITEM.getKey(item)) && (attribute.getWeight() > 0 || reforge)) {
                potentialAttributes.add(Identifier.parse(attribute.getID()));
                attributeWeights.add(reforge ? Math.max(1, attribute.getWeight() + 1) : attribute.getWeight());
            }
        });
        if (potentialAttributes.size() <= 0) {
            return null;
        }

        if (reforge && attributeWeights.size() > 2) {
            SortList.concurrentSort(attributeWeights, attributeWeights, potentialAttributes);
            int maxWeight = attributeWeights.get(attributeWeights.size() - 1);
            for (int i = 0; i < attributeWeights.size(); i++) {
                if (attributeWeights.get(i) > maxWeight / 2) {
                    attributeWeights.set(i, Math.max(1, (int) (attributeWeights.get(i) * Math.max(0.0f, ConfigInit.CONFIG.reforgeModifier))));
                }
            }
        }
        // Luck
        if (playerEntity != null) {
            int luckMaxWeight = Collections.max(attributeWeights);
            for (int i = 0; i < attributeWeights.size(); i++) {
                if (attributeWeights.get(i) > luckMaxWeight / 3) {
                    attributeWeights.set(i, Math.max(1, (int) (attributeWeights.get(i) * Math.max(0.0f, 1.0f - ConfigInit.CONFIG.luckReforgeModifier * playerEntity.getLuck()))));
                }
            }
        }

        int totalWeight = 0;
        for (Integer weight : attributeWeights) {
            totalWeight += weight.intValue();
        }
        if (totalWeight <= 0) {
            return potentialAttributes.get(RANDOM.nextInt(potentialAttributes.size()));
        }
        int randomChoice = RANDOM.nextInt(totalWeight);
        SortList.concurrentSort(attributeWeights, attributeWeights, potentialAttributes);

        for (int i = 0; i < attributeWeights.size(); i++) {
            if (randomChoice < attributeWeights.get(i)) {
                return potentialAttributes.get(i);
            }
            randomChoice -= attributeWeights.get(i);
        }
        return potentialAttributes.get(RANDOM.nextInt(potentialAttributes.size()));
    }

    public static TierComponent createTierComponent(Identifier tierId) {
        float durableFactor = -1f;
        int operation = 0;
        for (AttributeTemplate attributeTemplate : Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tierId).getAttributes()) {
            if (attributeTemplate.getAttributeTypeID().equals("tiered:generic.durable")) {
                durableFactor = (float) Math.round(attributeTemplate.getEntityAttributeModifier().amount() * 100.0f) / 100.0f;
                operation = attributeTemplate.getEntityAttributeModifier().operation().id();
                break;
            }
        }
        return new TierComponent(tierId.toString(), durableFactor, operation);
    }

    public static void setItemStackAttribute(@Nullable Player playerEntity, ItemStack stack, boolean reforge) {
        if (stack.get(Tiered.TIER) == null && !stack.is(TieredItemTags.MODIFIER_RESTRICTED)) {
            // attempt to get a random tier
            Identifier potentialAttributeID = ModifierUtils.getRandomAttributeIDFor(playerEntity, stack.getItem(), reforge);
            // found an ID
            if (potentialAttributeID != null) {

                stack.set(Tiered.TIER, createTierComponent(potentialAttributeID));
            }
        }
    }

    public static void removeItemStackAttribute(ItemStack itemStack) {
        if (itemStack.get(Tiered.TIER) != null) {
            itemStack.remove(Tiered.TIER);
        }
    }

    @Nullable
    public static Identifier getAttributeId(ItemStack itemStack) {
        if (itemStack.get(Tiered.TIER) != null) {
            return Identifier.tryParse(itemStack.get(Tiered.TIER).tier());
        }
        return null;
    }

    public static void applyTierModifiers(Map<Identifier, PotentialAttribute> itemAttributes, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable EquipmentSlotGroup attributeModifierSlot,
            BiConsumer<Holder<Attribute>, AttributeModifier> attributeModifierConsumer) {
        if (itemStack.get(Tiered.TIER) == null) {
            return;
        }
        PotentialAttribute potentialAttribute = itemAttributes.get(getAttributeId(itemStack));
        if (potentialAttribute == null) {
            return;
        }
        for (AttributeTemplate template : potentialAttribute.getAttributes()) {
            if (template.getRequiredEquipmentSlots() != null) {
                if (equipmentSlot != null && Arrays.asList(template.getRequiredEquipmentSlots()).contains(equipmentSlot)) {
                    template.applyModifiers(equipmentSlot, attributeModifierConsumer);
                } else if (attributeModifierSlot != null) {
                    Optional<EquipmentSlot> optional = Arrays.stream(template.getRequiredEquipmentSlots()).filter(attributeModifierSlot::test).findFirst();
                    if (optional.isPresent() && Tiered.isPreferredEquipmentSlot(itemStack, optional.get())) {
                        template.applyModifiers(optional.get(), attributeModifierConsumer);
                    }
                }
            }
            if (template.getOptionalEquipmentSlots() != null) {
                if (equipmentSlot != null && Arrays.asList(template.getOptionalEquipmentSlots()).contains(equipmentSlot) && Tiered.isPreferredEquipmentSlot(itemStack, equipmentSlot)) {
                    template.applyModifiers(equipmentSlot, attributeModifierConsumer);
                } else if (attributeModifierSlot != null && attributeModifierSlot != EquipmentSlotGroup.ANY && attributeModifierSlot != EquipmentSlotGroup.HAND) {
                    Optional<EquipmentSlot> optional = Arrays.stream(template.getOptionalEquipmentSlots()).filter(attributeModifierSlot::test).findFirst();
                    if (optional.isPresent() && Tiered.isPreferredEquipmentSlot(itemStack, optional.get())) {
                        template.applyModifiers(optional.get(), attributeModifierConsumer);
                    }
                }
            }
        }
    }

    public static void refreshPlayerTiers(ServerPlayer serverPlayerEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            applyTierModifiers(Tiered.ATTRIBUTE_DATA_LOADER.getPreviousItemAttributes(), serverPlayerEntity.getItemBySlot(equipmentSlot), equipmentSlot, null, (attribute, modifier) -> {
                AttributeInstance instance = serverPlayerEntity.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(modifier.id());
                }
            });
        }
        updateItemStackComponent(serverPlayerEntity.getInventory());
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            applyTierModifiers(Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes(), serverPlayerEntity.getItemBySlot(equipmentSlot), equipmentSlot, null, (attribute, modifier) -> {
                AttributeInstance instance = serverPlayerEntity.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(modifier.id());
                    instance.addTransientModifier(modifier);
                }
            });
        }
        if (serverPlayerEntity.getHealth() > serverPlayerEntity.getMaxHealth()) {
            serverPlayerEntity.setHealth(serverPlayerEntity.getMaxHealth());
        }
    }

    public static void updateItemStackComponent(Inventory playerInventory) {
        for (int u = 0; u < playerInventory.getContainerSize(); u++) {
            ItemStack itemStack = playerInventory.getItem(u);
            if (!itemStack.isEmpty() && itemStack.get(Tiered.TIER) != null) {

                // Check if attribute exists
                List<Identifier> attributeIds = new ArrayList<>();
                Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
                    if (attribute.isValid(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))) {
                        attributeIds.add(id);
                    }
                });
                Identifier currentId = getAttributeId(itemStack);
                Identifier attributeID = null;
                if (currentId != null && attributeIds.contains(currentId)) {
                    attributeID = currentId;
                } else if (!attributeIds.isEmpty()) {
                    ModifierUtils.removeItemStackAttribute(itemStack);
                    attributeID = ModifierUtils.getRandomAttributeIDFor(null, itemStack.getItem(), false);
                } else {
                    ModifierUtils.removeItemStackAttribute(itemStack);
                    playerInventory.setItem(u, itemStack);
                }

                // found an ID
                if (attributeID != null) {
                    itemStack.set(Tiered.TIER, createTierComponent(attributeID));
                    playerInventory.setItem(u, itemStack);
                }
            }
        }
    }

}