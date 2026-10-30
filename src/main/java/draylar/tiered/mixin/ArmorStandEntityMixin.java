package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        nbt.putBoolean("TieredChecked", true);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        ArmorStandEntity armorStand = (ArmorStandEntity) (Object) this;
        if (nbt.getBoolean("TieredChecked") || nbt.contains("IsGenerated") || armorStand.getWorld().isClient() || !ConfigInit.CONFIG.entityItemModifier) {
            return;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = armorStand.getEquippedStack(equipmentSlot);
            if (!itemStack.isEmpty()) {
                ModifierUtils.setItemStackAttribute(null, itemStack, false);
            }
        }
    }

}
