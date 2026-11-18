package draylar.tiered.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.libz.api.Tab;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin implements Tab {

    @Override
    public Class<?> getParentScreenClass() {
        return this.getClass();
    }

}
