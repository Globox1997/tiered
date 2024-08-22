package draylar.tiered.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import draylar.tiered.util.AttributeHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;

import java.util.List;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {

    @Inject(method = "shootAll", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/RangedWeaponItem;createArrowEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/projectile/ProjectileEntity;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void shootAllMixin(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo info, float f, float g, float h, float i, int j, ItemStack itemStack, float k, ProjectileEntity projectileEntity) {
        if (projectileEntity instanceof PersistentProjectileEntity persistentProjectileEntity && persistentProjectileEntity.getOwner() instanceof PlayerEntity playerEntity) {
            persistentProjectileEntity.setDamage(AttributeHelper.getExtraCritDamage(playerEntity, (float) persistentProjectileEntity.getDamage()));
        }
    }


}
