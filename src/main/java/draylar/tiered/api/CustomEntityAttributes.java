package draylar.tiered.api;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class CustomEntityAttributes {

    public static final RegistryEntry<EntityAttribute> DIG_SPEED = register("tiered:generic.dig_speed", new ClampedEntityAttribute("attribute.name.generic.dig_speed", 0.0D, 0.0D, 2048.0D).setTracked(true));
    public static final RegistryEntry<EntityAttribute> CRIT_CHANCE = register("tiered:generic.crit_chance",
            new ClampedEntityAttribute("attribute.name.generic.crit_chance", 0.0D, 0.0D, 1D).setTracked(true));
    public static final RegistryEntry<EntityAttribute> DURABLE = register("tiered:generic.durable", new ClampedEntityAttribute("attribute.name.generic.durable", 0.0D, 0.0D, 1D).setTracked(true));
    public static final RegistryEntry<EntityAttribute> RANGE_ATTACK_DAMAGE = register("tiered:generic.range_attack_damage",
            new ClampedEntityAttribute("attribute.name.generic.range_attack_damage", 0.0D, 0.0D, 2048.0D).setTracked(true));

    public static void init() {
        // NO-OP
    }

    private static RegistryEntry<EntityAttribute> register(String id, EntityAttribute attribute) {
        return Registry.registerReference(Registries.ATTRIBUTE, Identifier.of(id), attribute);
    }
}
