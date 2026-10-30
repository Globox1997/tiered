package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import draylar.tiered.util.AttributeHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;

@Mixin(ThrownTrident.class)
public abstract class TridentEntityMixin extends AbstractArrow {

    public TridentEntityMixin(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyVariable(method = "onHitEntity", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"), ordinal = 0)
    private float onEntityHitMixin(float original) {
        if (this.getOwner() instanceof ServerPlayer serverPlayerEntity) {
            return AttributeHelper.getExtraRangeDamage(serverPlayerEntity, this.getWeaponItem(), original);
        }
        return original;
    }
}
