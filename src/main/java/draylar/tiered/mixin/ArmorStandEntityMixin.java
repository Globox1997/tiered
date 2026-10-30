package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

@Mixin(ArmorStand.class)
public abstract class ArmorStandEntityMixin {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(CompoundTag nbt, CallbackInfo info) {
        nbt.putBoolean("TieredChecked", true);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(CompoundTag nbt, CallbackInfo info) {
        ArmorStand armorStand = (ArmorStand) (Object) this;
        if (nbt.getBoolean("TieredChecked") || nbt.contains("IsGenerated") || armorStand.level().isClientSide() || !ConfigInit.CONFIG.entityItemModifier) {
            return;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = armorStand.getItemBySlot(equipmentSlot);
            if (!itemStack.isEmpty()) {
                ModifierUtils.setItemStackAttribute(null, itemStack, false);
            }
        }
    }

}
