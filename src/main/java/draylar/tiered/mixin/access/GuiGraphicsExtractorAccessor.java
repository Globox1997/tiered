package draylar.tiered.mixin.access;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {

    @Nullable
    @Accessor("deferredTooltip")
    Runnable getDeferredTooltip();

    @Accessor("deferredTooltip")
    void setDeferredTooltip(@Nullable Runnable deferredTooltip);
}
