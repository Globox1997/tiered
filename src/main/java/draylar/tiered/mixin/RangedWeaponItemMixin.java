package draylar.tiered.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import draylar.tiered.util.AttributeHelper;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

@Mixin(ProjectileWeaponItem.class)
public class RangedWeaponItemMixin {

    @Inject(method = "shoot", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void shootAllMixin(ServerLevel world, LivingEntity shooter, InteractionHand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo info, float f, float g, float h, float i, int j, ItemStack itemStack, float k, Projectile projectileEntity) {
        if (projectileEntity instanceof AbstractArrow persistentProjectileEntity && persistentProjectileEntity.getOwner() instanceof Player playerEntity) {
            float damage = AttributeHelper.getExtraRangeDamage(playerEntity, (float) persistentProjectileEntity.getBaseDamage());
            persistentProjectileEntity.setBaseDamage(AttributeHelper.getExtraCritDamage(playerEntity, damage));
        }
    }


}
