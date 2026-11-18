package draylar.tiered.mixin.compat;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import fuzs.easyanvils.common.client.gui.screens.inventory.ModAnvilScreen;
import net.libz.api.Tab;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

@Mixin(ModAnvilScreen.class)
public class ModAnvilScreenMixin implements Tab {

    @Override
    public @Nullable Class<?> getParentScreenClass() {
        return AnvilScreen.class;
    }
}
