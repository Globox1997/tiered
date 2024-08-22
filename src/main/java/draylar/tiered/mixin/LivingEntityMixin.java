package draylar.tiered.mixin;

import draylar.tiered.Tiered;
import draylar.tiered.mixin.access.ServerPlayerEntityAccessor;
import draylar.tiered.network.TieredServerPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@SuppressWarnings("rawtypes")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Mutable
    @Shadow
    @Final
    private static TrackedData<Float> HEALTH;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Item attributes aren't applied until the player first ticks, which means any attributes such as bonus health are reset. This is annoying with health boosting armor.
     */
    @Redirect(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    private void readCustomDataFromNbtMixin(LivingEntity livingEntity, float health) {
        this.dataTracker.set(HEALTH, health);
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", shift = Shift.AFTER, ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getEquipmentChangesMixin(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> info, Map map, EquipmentSlot var2[], int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.get(Tiered.TIER) != null && (Object) this instanceof ServerPlayerEntity serverPlayerEntity) {
            this.setHealth(this.getHealth() > this.getMaxHealth() ? this.getMaxHealth() : this.getHealth());
            TieredServerPacket.writeS2CHealthPacket(serverPlayerEntity);
            ((ServerPlayerEntityAccessor)serverPlayerEntity).setSyncedHealth(serverPlayerEntity.getHealth());

        }
    }

    @Shadow
    public float getHealth() {
        return 0f;
    }

    @Shadow
    public final float getMaxHealth() {
        return 0;
    }

    @Shadow
    public void setHealth(float health) {
    }

}
