package draylar.tiered.mixin.compat;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import fuzs.easyanvils.client.gui.screens.inventory.ModAnvilScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

@Environment(EnvType.CLIENT)
@Mixin(ModAnvilScreen.class)
public class ModAnvilScreenMixin implements Tab {

    @Override
    public @Nullable Class<?> getParentScreenClass() {
        return AnvilScreen.class;
    }
}
