package draylar.tiered.api;

import draylar.tiered.Tiered;
import draylar.tiered.config.ConfigInit;
import net.levelz.access.LevelManagerAccess;
import net.levelz.level.LevelManager;
import net.levelz.level.Skill;
import net.libz.util.SortList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

public class ModifierUtils {

    /**
     * Returns the ID of a random attribute that is valid for the given {@link Item} in {@link Identifier} form.
     * <p>
     * If there is no valid attribute for the given {@link Item}, null is returned.
     *
     * @param item {@link Item} to generate a random attribute for
     * @return id of random attribute for item in {@link Identifier} form, or null if there are no valid options
     */
    @Nullable
    public static Identifier getRandomAttributeIDFor(@Nullable PlayerEntity playerEntity, Item item, boolean reforge) {
        List<Identifier> potentialAttributes = new ArrayList<>();
        List<Integer> attributeWeights = new ArrayList<>();
        // collect all valid attributes for the given item and their weights

        Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
            if (attribute.isValid(Registries.ITEM.getId(item)) && (attribute.getWeight() > 0 || reforge)) {
                potentialAttributes.add(Identifier.of(attribute.getID()));
                attributeWeights.add(reforge ? attribute.getWeight() + 1 : attribute.getWeight());
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
                    attributeWeights.set(i, (int) (attributeWeights.get(i) * ConfigInit.CONFIG.reforgeModifier));
                }
            }
        }
        // LevelZ
        if (Tiered.isLevelZLoaded && playerEntity != null) {
            LevelManager.SKILLS.values().stream().filter(skill -> skill.getKey().equals("smithing"));
            for (Skill skill : LevelManager.SKILLS.values()) {
                if (skill.getKey().equals("smithing")) {
                    int newMaxWeight = Collections.max(attributeWeights);
                    for (int i = 0; i < attributeWeights.size(); i++) {
                        if (attributeWeights.get(i) > newMaxWeight / 3) {
                            attributeWeights.set(i, (int) (attributeWeights.get(i)
                                    * (1.0f - ConfigInit.CONFIG.levelzReforgeModifier * ((LevelManagerAccess) playerEntity).getLevelManager().getSkillLevel(skill.getId()))));
                        }
                    }
                    break;
                }
            }

        }
        // Luck
        if (playerEntity != null) {
            int luckMaxWeight = Collections.max(attributeWeights);
            for (int i = 0; i < attributeWeights.size(); i++) {
                if (attributeWeights.get(i) > luckMaxWeight / 3) {
                    attributeWeights.set(i, (int) (attributeWeights.get(i) * (1.0f - ConfigInit.CONFIG.luckReforgeModifier * playerEntity.getLuck())));
                }
            }
        }

        if (potentialAttributes.size() > 0) {
            int totalWeight = 0;
            for (Integer weight : attributeWeights) {
                totalWeight += weight.intValue();
            }
            int randomChoice = new Random().nextInt(totalWeight);
            SortList.concurrentSort(attributeWeights, attributeWeights, potentialAttributes);

            for (int i = 0; i < attributeWeights.size(); i++) {
                if (randomChoice < attributeWeights.get(i)) {
                    return potentialAttributes.get(i);
                }
                randomChoice -= attributeWeights.get(i);
            }
            // If random choice didn't work
            return potentialAttributes.get(new Random().nextInt(potentialAttributes.size()));
        } else
            return null;
    }

    public static void setItemStackAttribute(@Nullable PlayerEntity playerEntity, ItemStack stack, boolean reforge) {
        if (stack.get(Tiered.TIER) == null && !stack.isIn(TieredItemTags.MODIFIER_RESTRICTED)) {
            // attempt to get a random tier
            Identifier potentialAttributeID = ModifierUtils.getRandomAttributeIDFor(playerEntity, stack.getItem(), reforge);
            // found an ID
            if (potentialAttributeID != null) {

                // add durability nbt
                float durableFactor = -1f;
                int operation = 0;
                List<AttributeTemplate> attributeList = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(Identifier.of(potentialAttributeID.toString())).getAttributes();
                for (AttributeTemplate attributeTemplate : attributeList) {
                    if (attributeTemplate.getAttributeTypeID().equals("tiered:generic.durable")) {
                        durableFactor = (float) Math.round(attributeTemplate.getEntityAttributeModifier().value() * 100.0f) / 100.0f;
                        operation = attributeTemplate.getEntityAttributeModifier().operation().getId();
                        break;
                    }
                }
                stack.set(Tiered.TIER, new TierComponent(potentialAttributeID.toString(), durableFactor, operation));
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
            return Identifier.of(itemStack.get(Tiered.TIER).tier());
        }
        return null;
    }

    public static void updateItemStackComponent(PlayerInventory playerInventory) {
        for (int u = 0; u < playerInventory.size(); u++) {
            ItemStack itemStack = playerInventory.getStack(u);
            if (!itemStack.isEmpty() && itemStack.get(Tiered.TIER) != null) {

                // Check if attribute exists
                List<String> attributeIds = new ArrayList<>();
                Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().forEach((id, attribute) -> {
                    if (attribute.isValid(Registries.ITEM.getId(itemStack.getItem()))) {
                        attributeIds.add(attribute.getID());
                    }
                });
                Identifier attributeID = null;
                for (int i = 0; i < attributeIds.size(); i++) {
                    if (itemStack.get(Tiered.TIER).tier().contains(attributeIds.get(i))) {
                        attributeID = Identifier.of(attributeIds.get(i));
                        break;
                    } else if (i == attributeIds.size() - 1) {
                        ModifierUtils.removeItemStackAttribute(itemStack);
                        attributeID = ModifierUtils.getRandomAttributeIDFor(null, itemStack.getItem(), false);
                    }
                }

                // found an ID
                if (attributeID != null) {
                    // update durability nbt
                    float durableFactor = -1f;
                    int operation = 0;
                    List<AttributeTemplate> attributeList = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(Identifier.of(attributeID.toString())).getAttributes();
                    for (int i = 0; i < attributeList.size(); i++) {
                        if (attributeList.get(i).getAttributeTypeID().equals("tiered:generic.durable")) {
                            durableFactor = (float) Math.round(attributeList.get(i).getEntityAttributeModifier().value() * 100.0f) / 100.0f;
                            operation = attributeList.get(i).getEntityAttributeModifier().operation().getId();
                            break;
                        }
                    }

                    itemStack.set(Tiered.TIER, new TierComponent(attributeID.toString(), durableFactor, operation));
                    playerInventory.setStack(u, itemStack);
                }
            }
        }
    }

}