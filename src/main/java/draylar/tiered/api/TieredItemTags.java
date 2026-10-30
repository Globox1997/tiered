package draylar.tiered.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

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
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tiered", id));
    }
}
