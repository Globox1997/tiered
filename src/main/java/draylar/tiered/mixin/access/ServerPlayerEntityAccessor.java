package draylar.tiered.mixin.access;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerEntityAccessor {

    @Accessor("lastSentHealth")
    void setSyncedHealth(float syncedHealth);
}
