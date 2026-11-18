package draylar.tiered.api;

import com.google.common.collect.Multimap;
import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Stores information on an AttributeModifier template applied to an ItemStack.
 * <p>
 * The ID of the AttributeTemplate is the logical ID used to determine what "type" of attribute of is. An EntityAttributeModifier has: - a UUID, which is a unique identifier to separate different
 * attributes of the same type - a name, which is used for generating a non-specified UUID and displaying in tooltips in some context - an amount, which is used in combination with the operation to
 * modify the final relevant value - a modifier, which can be something such as ADD_VALUE or subtraction
 * <p>
 * The EquipmentSlot is used to only apply this template to certain items.
 */
public class AttributeTemplate {

    @SerializedName("type")
    private final String attributeTypeID;

    @SerializedName("modifier")
    private final AttributeModifier entityAttributeModifier;

    @SerializedName("required_equipment_slots")
    private final EquipmentSlot[] requiredEquipmentSlots;

    @SerializedName("optional_equipment_slots")
    private final EquipmentSlot[] optionalEquipmentSlots;

    public AttributeTemplate(String attributeTypeID, AttributeModifier entityAttributeModifier, EquipmentSlot[] requiredEquipmentSlots, EquipmentSlot[] optionalEquipmentSlots) {
        this.attributeTypeID = attributeTypeID;
        this.entityAttributeModifier = entityAttributeModifier;
        this.requiredEquipmentSlots = requiredEquipmentSlots;
        this.optionalEquipmentSlots = optionalEquipmentSlots;
    }

    public EquipmentSlot[] getRequiredEquipmentSlots() {
        return requiredEquipmentSlots;
    }

    public EquipmentSlot[] getOptionalEquipmentSlots() {
        return optionalEquipmentSlots;
    }

    public AttributeModifier getEntityAttributeModifier() {
        return entityAttributeModifier;
    }

    public String getAttributeTypeID() {
        return attributeTypeID;
    }

    /**
     * Uses this {@link AttributeTemplate} to create an {@link AttributeModifier}, which is placed into the given {@link Multimap}.
     * <p>
     * Note that this method assumes the given {@link Multimap} is mutable.
     *
     * @param attributeConsumer biconsumer to accept {@link AttributeTemplate}
     * @param slot
     */
    public void applyModifiers(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> attributeConsumer) {
        Optional<Holder.Reference<Attribute>> optional = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(this.attributeTypeID));

        if (optional.isPresent()) {
            AttributeModifier cloneModifier = new AttributeModifier(Identifier.parse(entityAttributeModifier.id().toString() + "_" + slot.getName()), entityAttributeModifier.amount(), entityAttributeModifier.operation());

            ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(optional.get(), cloneModifier, EquipmentSlotGroup.bySlot(slot));
            if (entry.slot().test(slot)) {
                attributeConsumer.accept(entry.attribute(), entry.modifier());
            }
        }
    }
}
