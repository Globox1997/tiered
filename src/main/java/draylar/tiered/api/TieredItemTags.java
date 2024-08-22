package draylar.tiered.api;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TieredItemTags {

    public static final TagKey<Item> MODIFIER_RESTRICTED = register("modifier_restricted");
    public static final TagKey<Item> REFORGE_ADDITION = register("reforge_addition");
    public static final TagKey<Item> REFORGE_BASE_ITEM = register("reforge_base_item");
    public static final TagKey<Item> MAIN_OFFHAND_ITEM = register("main_offhand_item");

    private TieredItemTags() {
    }

    public static void init() {
    }

    private static TagKey<Item> register(String id) {
        return TagKey.of(RegistryKeys.ITEM, Identifier.of("tiered", id));
    }
}
