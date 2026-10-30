package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.api.ModifierUtils;
import draylar.tiered.config.ConfigInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ItemFrame.class)
public abstract class ItemFrameEntityMixin extends HangingEntity {

    public ItemFrameEntityMixin(EntityType<? extends HangingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    private boolean tieredChecked = false;

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readCustomDataFromNbtMixin(CompoundTag nbt, CallbackInfo info) {
        this.tieredChecked = nbt.getBoolean("TieredChecked");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(CompoundTag nbt, CallbackInfo info) {
        nbt.putBoolean("TieredChecked", true);
    }

    @Inject(method = "setItem(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;onItemChanged(Lnet/minecraft/world/item/ItemStack;)V"))
    private void setHeldItemStackMixin(ItemStack value, boolean update, CallbackInfo info) {
        if (!this.level().isClientSide() && !update && !this.tieredChecked && ConfigInit.CONFIG.lootContainerModifier) {
            ModifierUtils.setItemStackAttribute(null, value, false);
        }
        this.tieredChecked = true;
    }
}
