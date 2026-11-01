package draylar.tiered.api;

import draylar.tiered.Tiered;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemVerifier {

    private final String id;
    private final String tag;

    public ItemVerifier(String id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    /**
     * Returns whether the given {@link Identifier} is valid for this ItemVerifier, which may check direct against either a {@link Identifier} or {@link TagKey<Item>}.
     * <p>
     * The given {@link Identifier} should be the ID of an {@link Item} in {@link BuiltInRegistries#ITEM}.
     *
     * @param itemID item registry ID to check against this verifier
     * @return whether the check succeeded
     */
    public boolean isValid(Identifier itemID) {
        return isValid(itemID.toString());
    }

    /**
     * Returns whether the given {@link String} is valid for this ItemVerifier, which may check direct against either a {@link Identifier} or {@link TagKey<Item>}.
     * <p>
     * The given {@link String} should be the ID of an {@link Item} in {@link BuiltInRegistries#ITEM}.
     *
     * @param itemID item registry ID to check against this verifier
     * @return whether the check succeeded
     */
    @SuppressWarnings("deprecation")
    public boolean isValid(String itemID) {
        if (id != null) {
            return itemID.equals(id);
        } else if (tag != null) {
            TagKey<Item> itemTag = TagKey.create(Registries.ITEM, Identifier.parse(tag));
            if (itemTag != null) {
                return BuiltInRegistries.ITEM.getValue(Identifier.parse(itemID)).builtInRegistryHolder().is(itemTag);
            } else {
                Tiered.LOGGER.error(tag + " was specified as an item verifier tag, but it does not exist!");
            }
        }

        return false;
    }

    public String getId() {
        return id;
    }

    public TagKey<Item> getTagKey() {
        return TagKey.create(Registries.ITEM, Identifier.parse(tag));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ItemVerifier other)) {
            return false;
        }
        return Objects.equals(this.id, other.id) && Objects.equals(this.tag, other.tag);
    }
}
