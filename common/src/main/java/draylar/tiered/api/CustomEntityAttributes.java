package draylar.tiered.api;

import draylar.tiered.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class CustomEntityAttributes {

    public static final Holder<Attribute> DIG_SPEED = register("tiered:generic.dig_speed", new RangedAttribute("attribute.name.generic.dig_speed", 0.0D, 0.0D, 2048.0D).setSyncable(true));
    public static final Holder<Attribute> CRIT_CHANCE = register("tiered:generic.crit_chance",
            new RangedAttribute("attribute.name.generic.crit_chance", 0.0D, 0.0D, 1D).setSyncable(true));
    public static final Holder<Attribute> DURABLE = register("tiered:generic.durable", new RangedAttribute("attribute.name.generic.durable", 0.0D, 0.0D, 1D).setSyncable(true));
    public static final Holder<Attribute> RANGE_ATTACK_DAMAGE = register("tiered:generic.range_attack_damage",
            new RangedAttribute("attribute.name.generic.range_attack_damage", 0.0D, 0.0D, 2048.0D).setSyncable(true));

    public static void init() {
        // NO-OP
    }

    private static Holder<Attribute> register(String id, Attribute attribute) {
        return Services.REGISTRY.register(Registries.ATTRIBUTE, Identifier.parse(id), () -> attribute);
    }
}
