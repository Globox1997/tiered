package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import draylar.tiered.mixin.access.AbstractArrowAccessor;
import draylar.tiered.util.AttributeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ProjectileWeaponItem;

@Mixin(ProjectileWeaponItem.class)
public class RangedWeaponItemMixin {

    @ModifyExpressionValue(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile shootAllMixin(Projectile projectileEntity) {
        if (projectileEntity instanceof AbstractArrow persistentProjectileEntity && persistentProjectileEntity.getOwner() instanceof Player playerEntity) {
            float damage = AttributeHelper.getExtraRangeDamage(playerEntity, (float) ((AbstractArrowAccessor) persistentProjectileEntity).getBaseDamage());
            persistentProjectileEntity.setBaseDamage(AttributeHelper.getExtraCritDamage(playerEntity, damage));
        }
        return projectileEntity;
    }

}
