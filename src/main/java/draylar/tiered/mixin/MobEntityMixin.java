package draylar.tiered.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(Mob.class)
public class MobEntityMixin {

    @Unique
    private boolean tieredPending = false;

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void initializeMixin(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> info) {
        this.tieredPending = true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickMixin(CallbackInfo info) {
        if (this.tieredPending && !((Entity) (Object) this).level().isClientSide()) {
            this.tieredPending = false;
            if (ConfigInit.CONFIG.entityItemModifier) {
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                    if (itemStack.isEmpty()) {
                        continue;
                    }
                    ModifierUtils.setItemStackAttribute(null, itemStack, false);
                }
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(CompoundTag nbt, CallbackInfo info) {
        if (this.tieredPending) {
            nbt.putBoolean("TieredPending", true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(CompoundTag nbt, CallbackInfo info) {
        this.tieredPending = nbt.getBoolean("TieredPending");
    }

    @Shadow
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return null;
    }

}
