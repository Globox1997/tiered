package draylar.tiered.fabric.mixin;

import draylar.tiered.api.CustomEntityAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerAttributesMixin {

    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void createPlayerAttributesMixin(CallbackInfoReturnable<AttributeSupplier.Builder> info) {
        info.getReturnValue().add(CustomEntityAttributes.CRIT_CHANCE);
        info.getReturnValue().add(CustomEntityAttributes.DIG_SPEED);
        info.getReturnValue().add(CustomEntityAttributes.DURABLE);
        info.getReturnValue().add(CustomEntityAttributes.RANGE_ATTACK_DAMAGE);
    }

}
