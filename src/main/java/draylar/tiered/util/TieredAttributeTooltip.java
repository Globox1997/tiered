package draylar.tiered.util;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

@Environment(EnvType.CLIENT)
public class TieredAttributeTooltip {

    @Nullable
    public static Map<Holder<Attribute>, List<AttributeModifier>> tieredMap;
    public static boolean slotInfo;

}
