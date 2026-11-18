package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.access.AnvilScreenHandlerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin implements AnvilScreenHandlerAccess {

    @Unique
    private BlockPos pos;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void initMixin(int syncId, Inventory inventory, ContainerLevelAccess context, CallbackInfo info) {
        context.execute((world, pos) -> {
            AnvilScreenHandlerMixin.this.setPos(pos);
        });

    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

}
