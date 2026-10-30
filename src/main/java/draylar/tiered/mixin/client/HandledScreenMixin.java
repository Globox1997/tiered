package draylar.tiered.mixin.client;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import draylar.tiered.api.BorderTemplate;
import draylar.tiered.util.TieredTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {

    public HandledScreenMixin(Component title) {
        super(title);
    }

    @WrapOperation(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"))
    protected void drawMouseoverTooltipMixin(GuiGraphics context, Font textRenderer, List<Component> text, Optional<TooltipComponent> data, int x, int y, Operation<Void> original, @Local ItemStack stack) {
        BorderTemplate borderTemplate = TieredTooltip.getBorderTemplate(stack);
        if (borderTemplate != null) {
            TieredTooltip.renderTieredTooltip(context, textRenderer, text, data, x, y, borderTemplate);
        } else {
            original.call(context, textRenderer, text, data, x, y);
        }
    }

}
