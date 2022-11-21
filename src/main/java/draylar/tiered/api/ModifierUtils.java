package draylar.tiered.api;

import draylar.tiered.Tiered;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.util.SortList;
import net.levelz.access.PlayerStatsManagerAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
            if (attribute.isValid(Registry.ITEM.getId(item)) && (attribute.getWeight() > 0 || reforge)) {
                potentialAttributes.add(new Identifier(attribute.getID()));
                attributeWeights.add(reforge ? attribute.getWeight() + 1 : attribute.getWeight());
            }
        });
        if (potentialAttributes.size() <= 0)
            return null;

        if (reforge && attributeWeights.size() > 2) {
            SortList.concurrentSort(attributeWeights, attributeWeights, potentialAttributes);
            int maxWeight = attributeWeights.get(attributeWeights.size() - 1);
            for (int i = 0; i < attributeWeights.size(); i++)
                if (attributeWeights.get(i) > maxWeight / 2)
                    attributeWeights.set(i, (int) (attributeWeights.get(i) * ConfigInit.CONFIG.reforge_modifier));
        }
        // LevelZ
        if (Tiered.isLevelZLoaded && playerEntity != null) {
            int newMaxWeight = Collections.max(attributeWeights);
            for (int i = 0; i < attributeWeights.size(); i++)
                if (attributeWeights.get(i) > newMaxWeight / 3)
                    attributeWeights.set(i, (int) (attributeWeights.get(i)
                            * (1.0f - ConfigInit.CONFIG.levelz_reforge_modifier * ((PlayerStatsManagerAccess) playerEntity).getPlayerStatsManager().getLevel("smithing"))));
        }
        // Luck
        if (playerEntity != null) {
            int luckMaxWeight = Collections.max(attributeWeights);
            for (int i = 0; i < attributeWeights.size(); i++)
                if (attributeWeights.get(i) > luckMaxWeight / 3)
                    attributeWeights.set(i, (int) (attributeWeights.get(i) * (1.0f - ConfigInit.CONFIG.luck_reforge_modifier * playerEntity.getLuck())));
        }

        if (potentialAttributes.size() > 0) {
            int totalWeight = 0;
            for (Integer weight : attributeWeights)
                totalWeight += weight.intValue();
            int randomChoice = new Random().nextInt(totalWeight);
            SortList.concurrentSort(attributeWeights, attributeWeights, potentialAttributes);

            for (int i = 0; i < attributeWeights.size(); i++) {
                if (randomChoice < attributeWeights.get(i))
                    return potentialAttributes.get(i);
                randomChoice -= attributeWeights.get(i);
            }
            // If random choice didn't work
            return potentialAttributes.get(new Random().nextInt(potentialAttributes.size()));
        } else
            return null;
    }

    public static void setItemStackAttribute(@Nullable PlayerEntity playerEntity, ItemStack stack, boolean reforge) {
        if (stack.getSubNbt(Tiered.NBT_SUBTAG_KEY) == null) {

            // attempt to get a random tier
            Identifier potentialAttributeID = ModifierUtils.getRandomAttributeIDFor(playerEntity, stack.getItem(), reforge);
            // found an ID
            if (potentialAttributeID != null) {
                stack.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).putString(Tiered.NBT_SUBTAG_DATA_KEY, potentialAttributeID.toString());

                PotentialAttribute.Template template = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(new Identifier(potentialAttributeID.toString())).getTemplate(Registry.ITEM.getId(stack.getItem()));
                if (StringUtils.isNotBlank(template.getId())) {
                    stack.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).putString(Tiered.NBT_SUBTAG_TEMPLATE_DATA_KEY, template.getId());
                }
                HashMap<String, Object> nbtMap = template.getNbtValues();

                // add durability nbt
                List<AttributeTemplate> attributeList = template.getAttributes();
                for (AttributeTemplate attributeTemplate : attributeList)
                    if (attributeTemplate.getAttributeTypeID().equals("tiered:generic.durable")) {
                        if (nbtMap == null)
                            nbtMap = new HashMap<String, Object>();
                        nbtMap.put("durable", (double) Math.round(attributeTemplate.getEntityAttributeModifier().getValue() * 100.0) / 100.0);
                        break;
                    }
                // add nbtMap
                if (nbtMap != null) {
                    NbtCompound nbtCompound = stack.getNbt();
                    for (HashMap.Entry<String, Object> entry : nbtMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();

                        // json list will get read as ArrayList class
                        // json map will get read as linkedtreemap
                        // json integer is read by gson -> always double
                        if (value instanceof String)
                            nbtCompound.putString(key, (String) value);
                        else if (value instanceof Boolean)
                            nbtCompound.putBoolean(key, (boolean) value);
                        else if (value instanceof Double) {
                            if ((double) value % 1.0 < 0.0001D)
                                nbtCompound.putInt(key, (int) Math.round((double) value));
                            else
                                nbtCompound.putDouble(key, Math.round((double) value * 100.0) / 100.0);
                        }
                    }
                    stack.setNbt(nbtCompound);
                }
            }
        }
    }

    public static void removeItemStackAttribute(ItemStack itemStack) {
        if (itemStack.hasNbt() && itemStack.getSubNbt(Tiered.NBT_SUBTAG_KEY) != null) {

            Identifier tier = new Identifier(itemStack.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_DATA_KEY));
            String templateId = itemStack.getOrCreateSubNbt(Tiered.NBT_SUBTAG_KEY).getString(Tiered.NBT_SUBTAG_TEMPLATE_DATA_KEY);
            PotentialAttribute.Template template = Tiered.ATTRIBUTE_DATA_LOADER.getItemAttributes().get(tier).getTemplate(Registry.ITEM.getId(itemStack.getItem()), templateId);

            HashMap<String, Object> nbtMap = template.getNbtValues();
            List<String> nbtKeys = new ArrayList<String>();
            if (nbtMap != null)
                nbtKeys.addAll(nbtMap.keySet().stream().toList());

            List<AttributeTemplate> attributeList = template.getAttributes();
            for (int i = 0; i < attributeList.size(); i++)
                if (attributeList.get(i).getAttributeTypeID().equals("tiered:generic.durable")) {
                    nbtKeys.add("durable");
                    break;
                }

            if (!nbtKeys.isEmpty())
                for (int i = 0; i < nbtKeys.size(); i++)
                    if (!nbtKeys.get(i).equals("Damage"))
                        itemStack.getNbt().remove(nbtKeys.get(i));

            itemStack.removeSubNbt(Tiered.NBT_SUBTAG_KEY);
        }
    }

    private ModifierUtils() {
        // no-op
    }
}